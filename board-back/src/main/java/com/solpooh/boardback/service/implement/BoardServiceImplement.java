package com.solpooh.boardback.service.implement;

import com.solpooh.boardback.common.Pagination;
import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.BoardConverter;
import com.solpooh.boardback.converter.ImageConverter;
import com.solpooh.boardback.converter.SearchLogConverter;
import com.solpooh.boardback.dto.object.BoardListResponse;
import com.solpooh.boardback.dto.object.CommentResponse;
import com.solpooh.boardback.dto.object.FavoriteResponse;
import com.solpooh.boardback.dto.request.board.PatchBoardRequest;
import com.solpooh.boardback.dto.request.board.PatchCommentRequest;
import com.solpooh.boardback.dto.request.board.PostBoardRequest;
import com.solpooh.boardback.dto.request.board.PostCommentRequest;
import com.solpooh.boardback.dto.response.board.*;
import com.solpooh.boardback.entity.*;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.repository.*;
import com.solpooh.boardback.repository.resultSet.GetBoardDetailResultSet;
import com.solpooh.boardback.repository.resultSet.GetCommentListResultSet;
import com.solpooh.boardback.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImplement implements BoardService {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ImageRepository imageRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final SearchLogRepository searchLogRepository;
    private final BoardListViewRepository boardListViewRepository;

    @Override
    public GetBoardDetailResponse getBoardDetail(String category, Long boardNumber) {
        // 게시물 상세조회
        GetBoardDetailResultSet resultSet = boardRepository.getBoardDetail(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        // 포함된 이미지 조회
        List<String> boardImageList =
                imageRepository.findByBoardNumberAndDeleted(boardNumber, false)
                        .stream()
                        .map(ImageEntity::getImage)
                        .toList();

        return BoardConverter.toResponse(resultSet, boardImageList);
    }

    @Override
    public GetFavoriteListResponse getFavoriteList(Long boardNumber) {
        if (!boardRepository.existsByBoardNumber(boardNumber))
            throw new CustomException(ResponseApi.NOT_EXISTED_BOARD);

        List<FavoriteResponse> favoriteList =
                favoriteRepository.getFavoriteList(boardNumber)
                        .stream()
                        .map(BoardConverter::toResponse)
                        .toList();

        return new GetFavoriteListResponse(favoriteList);
    }

    @Override
    public GetCommentListResponse getCommentList(Long boardNumber, Pageable pageable) {
        if (!boardRepository.existsByBoardNumber(boardNumber))
            throw new CustomException(ResponseApi.NOT_EXISTED_BOARD);

        Page<GetCommentListResultSet> resultSets = commentRepository.getCommentList(boardNumber, pageable);
        List<CommentResponse> commentList = resultSets.getContent()
                .stream()
                .map(BoardConverter::toResponse)
                .toList();

        Pagination<CommentResponse> pagedList = Pagination.of(resultSets, commentList);

        return new GetCommentListResponse(pagedList);
    }

    @Override
    public IncreaseViewCountResponse increaseViewCount(Long boardNumber) {
        BoardEntity boardEntity = boardRepository.findByBoardNumber(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));
        // 조회수 증가
        boardEntity.increaseViewCount();
        boardRepository.save(boardEntity);

        return new IncreaseViewCountResponse();
    }

    @Override
    public GetLatestBoardListResponse getLatestBoardList(String category, Pageable pageable) {
        // 페이징 게시글 조회
        Page<BoardListViewEntity> boardListViewEntities =
                ("All".equals(category) || category.isEmpty())
                        ? boardListViewRepository.findByOrderByWriteDatetimeDesc(pageable)
                        : boardListViewRepository.findByCategoryOrderByWriteDatetimeDesc(category, pageable);

        List<BoardListResponse> boardList = boardListViewEntities.getContent()
                .stream()
                .map(BoardConverter::toResponse)
                .toList();

        // 카테고리 카운트 조회
        List<CategoryResponse> categoryCounts = boardListViewRepository.findCategoryCount();

        Pagination<BoardListResponse> pagedList = Pagination.of(boardListViewEntities, boardList);

        return new GetLatestBoardListResponse(pagedList, categoryCounts);
    }

    @Override
    public GetTop3BoardListResponse getTop3BoardList() {
        // 일주일 전 날짜 계산
        Date beforeWeek = Date.from(Instant.now().minus(7, ChronoUnit.DAYS));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sevenDaysAgo = simpleDateFormat.format(beforeWeek);

        List<BoardListViewEntity> boardListViewEntities =
                boardListViewRepository.findTop3ByWriteDatetimeGreaterThanOrderByFavoriteCountDescCommentCountDescViewCountDescWriteDatetimeDesc(sevenDaysAgo);

        List<BoardListResponse> top3List = boardListViewEntities.stream()
                .map(BoardConverter::toResponse)
                .toList();

        return new GetTop3BoardListResponse(top3List);
    }

    @Override
    public GetSearchBoardListResponse getSearchBoardList(String searchWord, String preSearchWord, Pageable pageable) {
        // 1. 검색어로 게시글 목록 조회
        Page<BoardListViewEntity> boardListViewEntities =
                boardListViewRepository.findByTitleContainsOrContentContainsOrderByWriteDatetimeDesc(searchWord, searchWord, pageable);

        // 2. 조회 결과 DTO로 변환
        List<BoardListResponse> boardList = boardListViewEntities.getContent()
                .stream()
                .map(BoardConverter::toResponse)
                .toList();

        // 3. 검색 로그 저장 (보조 로직은 간결하게 분리하자)
        saveSearchLogs(searchWord, preSearchWord);

        // 4. 페이징 정보와 함께 응답 생성
        Pagination<BoardListResponse> pagedList = Pagination.of(boardListViewEntities, boardList);

        return new GetSearchBoardListResponse(pagedList);
    }

    private void saveSearchLogs(String searchWord, String preSearchWord) {
        // 기본 로그 저장
        searchLogRepository.save(SearchLogConverter.toEntity(searchWord, preSearchWord, false));

        // 연관 로그 저장 (preSearchWord가 있을 때만)
        if (preSearchWord != null) {
            searchLogRepository.save(SearchLogConverter.toEntity(preSearchWord, searchWord, true));
        }
    }

    @Override
    public GetUserBoardListResponse getUserBoardList(String email, Pageable pageable) {
        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        // 1. 유저별 게시물 목록 조회
        Page<BoardListViewEntity> boardListViewEntities =
                boardListViewRepository.findByWriterEmailOrderByWriteDatetimeDesc(email, pageable);

        // 2. 조회 결과 DTO로 변환
        List<BoardListResponse> boardList = boardListViewEntities.getContent()
                .stream()
                .map(BoardConverter::toResponse)
                .toList();

        // 3. 페이징 정보와 함께 응답 생성
        Pagination<BoardListResponse> pagedList = Pagination.of(boardListViewEntities, boardList);

        return new GetUserBoardListResponse(pagedList);
    }

    @Override
    public PostBoardResponse postBoard(PostBoardRequest dto, String email) {

        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        // 게시물 저장
        BoardEntity boardEntity = BoardConverter.toEntity(dto, email);
        boardRepository.save(boardEntity);

        // 이미지 저장
        Long boardNumber = boardEntity.getBoardNumber();
        List<ImageEntity> imageEntities = ImageConverter.toEntity(dto, boardNumber);

        imageRepository.saveAll(imageEntities);

        return new PostBoardResponse();
    }

    @Override
    public PostCommentResponse postComment(PostCommentRequest dto, Long boardNumber, String email) {
        BoardEntity boardEntity = boardRepository.findByBoardNumber(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        // 댓글 저장
        CommentEntity commentEntity = BoardConverter.toEntity(dto, boardNumber, email);
        commentRepository.save(commentEntity);

        // 게시물 댓글 수 증가
        boardEntity.increaseCommentCount();
        boardRepository.save(boardEntity);

        return new PostCommentResponse();
    }

    @Override
    public PutFavoriteResponse putFavorite(Long boardNumber, String email) {
        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        BoardEntity boardEntity = boardRepository.findByBoardNumber(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        favoriteRepository.findByBoardNumberAndUserEmail(boardNumber, email)
                .ifPresentOrElse(
                        // 이미 존재하면 → 삭제 및 좋아요 감소
                        favorite -> {
                            favoriteRepository.delete(favorite);
                            boardEntity.decreaseFavoriteCount();
                        },
                        // 존재하지 않으면 → 생성 및 좋아요 증가
                        () -> {
                            FavoriteEntity newFavorite  = new FavoriteEntity(email, boardNumber);
                            favoriteRepository.save(newFavorite);
                            boardEntity.increaseFavoriteCount();
                        }
                );

        boardRepository.save(boardEntity);

        return new PutFavoriteResponse();
    }

    @Override
    public PatchBoardResponse patchBoard(PatchBoardRequest dto, Long boardNumber, String email) {
        BoardEntity boardEntity = boardRepository.findByBoardNumber(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        boolean isWriter = boardEntity.getWriterEmail().equals(email);
        if (!isWriter) throw new CustomException(ResponseApi.NO_PERMISSION);

        // 게시물 수정
        boardEntity.patchBoard(dto);
        boardRepository.save(boardEntity);

        // 기존 이미지 다 지우기
        imageRepository.imageToDelete(boardNumber);

        // 새로운 이미지 추가
        List<ImageEntity> imageEntities = ImageConverter.toEntity(dto, boardNumber);
        imageRepository.saveAll(imageEntities);

        return new PatchBoardResponse();
    }

    @Override
    public PatchCommentResponse patchComment(PatchCommentRequest dto, Long boardNumber, Long commentNumber, String email) {
        BoardEntity boardEntity = boardRepository.findByBoardNumber(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        CommentEntity commentEntity = commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_COMMENT));

        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        boolean isWriter = boardEntity.getWriterEmail().equals(email);
        if (!isWriter) throw new CustomException(ResponseApi.NO_PERMISSION);

        // 댓글 수정
        commentEntity.patchComment(dto);
        commentRepository.save(commentEntity);

        return new PatchCommentResponse();
    }


    @Override
    public DeleteBoardResponse deleteBoard(Long boardNumber, String email) {
        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        BoardEntity boardEntity = boardRepository.findByBoardNumber(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        boolean isWriter = boardEntity.getWriterEmail().equals(email);
        if (!isWriter) throw new CustomException(ResponseApi.NO_PERMISSION);

        // 연관된 모든 엔티티 삭제
        imageRepository.imageToDelete(boardNumber);
        commentRepository.deleteByBoardNumber(boardNumber);
        favoriteRepository.deleteByBoardNumber(boardNumber);

        boardRepository.delete(boardEntity);

        return new DeleteBoardResponse();
    }

    @Override
    public DeleteCommentResponse deleteComment(Long boardNumber, Long commentNumber, String email) {

        BoardEntity boardEntity = boardRepository.findByBoardNumber(boardNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_BOARD));

        commentRepository.findByCommentNumber(commentNumber)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_COMMENT));

        if (!userRepository.existsByEmail(email))
            throw new CustomException(ResponseApi.NOT_EXISTED_USER);

        boolean isWriter = boardEntity.getWriterEmail().equals(email);
        if (!isWriter) throw new CustomException(ResponseApi.NO_PERMISSION);

        // 댓글 삭제
        commentRepository.deleteByBoardNumberAndCommentNumber(boardNumber, commentNumber);

        // 게시물의 댓글 수 감소
        boardEntity.decreaseCommentCount();
        boardRepository.save(boardEntity);

        return new DeleteCommentResponse();
    }
}
