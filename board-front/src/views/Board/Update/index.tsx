import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {useBoardStore, useLoginUserStore} from 'stores';
import {AUTH_PATH, MAIN_PATH} from '../../../constants';
import {useNavigate, useParams} from 'react-router-dom';
import {useCookies} from 'react-cookie';
import {getBoardRequest} from 'apis';
import {GetBoardResponseDto} from 'apis/response/board';
import {ResponseDto} from 'apis/response';
import {convertUrlsToFile, extractImageUrls} from 'utils';
import {
    AtomicBlockUtils, ContentBlock,
    convertFromRaw,
    convertToRaw,
    EditorState,
    getDefaultKeyBinding,
    Modifier,
    RichUtils, SelectionState
} from 'draft-js';
import Editor from '@draft-js-plugins/editor';
import createToolbarPlugin, {
    Separator,
} from '@draft-js-plugins/static-toolbar';
import '@draft-js-plugins/static-toolbar/lib/plugin.css';
import {
    ItalicButton,
    BoldButton,
    UnderlineButton,
    CodeButton,
    HeadlineOneButton,
    HeadlineTwoButton,
    HeadlineThreeButton,
    UnorderedListButton,
    OrderedListButton,
    BlockquoteButton,
    CodeBlockButton,
} from '@draft-js-plugins/buttons';
import editorStyles from './editorStyles.module.css';
import {ColorButton} from '../../../components/ColorButton';
import {customStyleMap} from '../../../plugins';
import {ImageUrl} from 'types/interface';
import ImageBlock from 'components/ImageBlock';

//  플러그인 설정
const toolbarPlugin = createToolbarPlugin();
const { Toolbar } = toolbarPlugin;
const plugins = [toolbarPlugin];

//  component: 게시물 수정 화면 컴포넌트 //
export default function BoardWrite() {

    //  state: 제목 영역 요소 참조 상태 //
    const titleRef = useRef<HTMLTextAreaElement | null>(null);
    //  state: 본문 영역 요소 참조 상태 //
    const contentRef = useRef<HTMLTextAreaElement | null>(null);
    //  state: 이미지 입력 요소 참조 상태 //
    const imageInputRef = useRef<HTMLInputElement | null>(null);

    //  state: 게시물 번호 path variable 상태 //
    const { boardNumber } = useParams();
    //  state: 게시물 상태 //
    const { title, setTitle } = useBoardStore();
    const { content, setContent } = useBoardStore();
    const { category, setCategory } = useBoardStore();
    const { boardImageFileList, setBoardImageFileList } = useBoardStore();
    //  state: 로그인 유저 상태 //
    const { loginUser } = useLoginUserStore();

    //  state: 쿠키 상태 //
    const [cookies, setCookies] = useCookies();

    //  state: 게시물 이미지 미리보기 URL 상태 //
    const [imageUrls, setImageUrls] = useState<ImageUrl[]>([]);

    //  state: Editor State 상태 //
    const [editorState, setEditorState] = useState(EditorState.createEmpty());

    const editorRef = useRef<Editor | null>(null);

    //  function: EditorState 접근 함수 //
    const getEditorState = () => editorState;
    const setEditorStateHandler = (newState: EditorState) => setEditorState(newState);

    //  function: 네비게이트 함수 //
    const navigator = useNavigate();

    //  function: get board response 처리 함수 //
    const getBoardResponse = (responseBody: GetBoardResponseDto | ResponseDto | null) => {
        if (!responseBody) return;
        const { code } = responseBody;
        if (code === 'NB') alert('존재하지 않는 게시물입니다.');
        if (code === 'DBE') alert('데이터베이스 오류입니다.');
        if (code !== 'SU') {
            navigator(MAIN_PATH());
            return;
        }

        const { title, content, category, boardImageList, writerEmail } = responseBody as GetBoardResponseDto;

        // 1. JSON 파싱 후 entityMap 추출
        let entityMap = {};

        const parsedContent = JSON.parse(content); // 문자열을 JSON 객체로 변환
        entityMap = parsedContent.entityMap; // entityMap 추출

        // 2. entityMap에서 ImageUrl[] 변환
        const imageUrls = extractImageUrls(entityMap);
        console.log(imageUrls)
        setImageUrls(imageUrls);

        setTitle(title);
        setCategory(category);
        convertUrlsToFile(imageUrls).then(boardImageFileList => setBoardImageFileList(boardImageFileList));
        console.log(boardImageFileList)

        const contentState = convertFromRaw(JSON.parse(content));
        const editorState = EditorState.createWithContent(contentState); // ContentState -> EditorState
        setEditorState(editorState);

        if (!loginUser || loginUser.email !== writerEmail) {
            navigator(MAIN_PATH());
            return;
        }

        if (!contentRef.current) return;
        contentRef.current.style.height = 'auto';
        contentRef.current.style.height = `${contentRef.current.scrollHeight}px`;
    }

    //  function: CodeBlock에서 'Enter' 및 'Tab' 키 처리 함수 //
    const handleKeyCommand = (command: string): 'handled' | 'not-handled' => {
        const blockType = RichUtils.getCurrentBlockType(editorState);

        if (blockType === 'code-block') {
            const currentContent = editorState.getCurrentContent();
            const selection = editorState.getSelection();

            if (command === 'enter') {
                const newContent = Modifier.insertText(currentContent, selection, '\n');
                setEditorState(EditorState.push(editorState, newContent, 'insert-characters'));
                return 'handled';
            }

            if (command === 'tab') {
                const newContent = Modifier.replaceText(currentContent, selection, '    ');
                setEditorState(EditorState.push(editorState, newContent, 'insert-characters'));
                return 'handled';
            }
            if (command === 'shift-enter') {
                const newEditorState = EditorState.push(
                    editorState,
                    Modifier.splitBlock(currentContent, selection),
                    'split-block'
                );
                setEditorState(RichUtils.toggleBlockType(newEditorState, 'unstyled'));
                return 'handled';
            }
        }
        // 기본 동작 유지
        return RichUtils.handleKeyCommand(editorState, command)
            ? 'handled'
            : 'not-handled';
    };

    //  function: 키 바인딩 함수 //
    const keyBindingFn = (event: React.KeyboardEvent): string | undefined => {
        const blockType = RichUtils.getCurrentBlockType(editorState);

        if (blockType === 'code-block') {
            if (event.key === 'Enter' && event.shiftKey) return 'shift-enter'; // Code Block 벗어나기
            if (event.key === 'Enter') return 'enter';
            if (event.key === 'Tab') return 'tab';
        }
        // 기본 동작을 유지하기 위해 기본 키 바인딩 반환
        return getDefaultKeyBinding(event) || undefined;
    };

    //  event handler: 카테고리 변경 이벤트 처리  //
    const onCategoryChangeHandler = (event: ChangeEvent<HTMLSelectElement>) => {
        const { value } = event.target;
        setCategory(value);
    }
    //  event handler: 제목 변경 이벤트 처리 //
    const onTitleChangeHandler = (event: ChangeEvent<HTMLTextAreaElement>) => {
        const { value } = event.target;
        setTitle(value);
        if (!titleRef.current) return;
        titleRef.current.style.height = 'auto';
        titleRef.current.style.height = `${titleRef.current.scrollHeight}px`;
    }
    //  event handler: editor 내용 변경 이벤트 처리  //
    const onEditorChangeHandler = (newState: EditorState) => {
        setEditorState(newState);

        // JSON으로 저장해야 포맷팅 정보 포함 가능
        const rawContent = JSON.stringify(convertToRaw(newState.getCurrentContent()));
        setContent(rawContent);
    }
    //  event handler: 이미지 변경 이벤트 처리 //
    const onImageChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
        if (!event.target.files || !event.target.files.length) return;
        const files = event.target.files;

        const newImageUrls: ImageUrl[] = [...imageUrls]; // 기존 이미지 URL 복사
        const newBoardImageFileList = [...boardImageFileList]; // 요청 보낼 File 객체 리스트

        let newEditorState = editorState;

        Array.from(files).forEach((file) => {
            const id = `image_${Date.now()}`; // 고유 ID 생성
            const imageUrl = URL.createObjectURL(file);

            newImageUrls.push({ id, url: imageUrl });
            newBoardImageFileList.push({ id, file });

            // Entity 생성
            const contentStateWithEntity = newEditorState
                .getCurrentContent()
                .createEntity('IMAGE', 'IMMUTABLE', { src: imageUrl, id: id });
            const entityKey = contentStateWithEntity.getLastCreatedEntityKey();

            // AtomicBlock 삽입
            newEditorState = AtomicBlockUtils.insertAtomicBlock(
                EditorState.set(newEditorState, { currentContent: contentStateWithEntity }),
                entityKey,
                ' ', // AtomicBlock 뒤에 삽입할 공백
            );
        });

        setImageUrls(newImageUrls);
        setBoardImageFileList(newBoardImageFileList);
        setEditorState(newEditorState);

        if (!imageInputRef.current) return;
        // 똑같은 이미지 새로운 등록을 위해 초기화
        imageInputRef.current.value = '';
    }
    //  event handler: 이미지 업로드 버튼 클릭 이벤트 처리 //
    const onImageUploadButtonClickHandler = () => {
        if (!imageInputRef.current) return;
        imageInputRef.current.click();
    }
    //  event handler: 이미지 닫기 버튼 클릭 이벤트 처리 //
    const onImageCloseButtonClickHandler = (deleteId: string) => {
        if (!imageInputRef.current) return;
        imageInputRef.current.value = '';

        // 미리보기 이미지 URL 삭제
        const newImageUrls = imageUrls.filter((image) => image.id !== deleteId);
        setImageUrls(newImageUrls);

        // 업로드할 파일 객체 리스트에서 해당 파일 삭제
        const newBoardImageFileList = boardImageFileList.filter((file) => file.id !== deleteId);
        setBoardImageFileList(newBoardImageFileList);

        // EditorState에서 해당 이미지를 제거 (AtomicBlock 삭제)
        const contentState = editorState.getCurrentContent();
        const blockMap = contentState.getBlockMap();

        // blockMap에서 deleteId에 해당하는 블록 찾기
        const blockToDelete = blockMap.find((block) => {
            // @ts-ignore
            const entityKey = block.getEntityAt(0);
            if (entityKey) {
                const entityData = contentState.getEntity(entityKey).getData();
                return entityData?.id === deleteId;
            }
            return false;
        });


        // 삭제하려는 블록이 존재하지 않으면 종료
        if (!blockToDelete) {
            console.warn('Block not found for id:', deleteId);
            return;
        }

        const blockKey = blockToDelete.getKey();

        // 해당 블록의 SelectionState 생성
        const blockSelection = SelectionState.createEmpty(blockKey).merge({
            anchorOffset: 0,
            focusOffset: blockToDelete.getLength(),
        });

        // Modifier를 이용해 블록 삭제
        const contentStateWithoutBlock = Modifier.removeRange(
            contentState,
            blockSelection,
            'backward'
        );

        // 삭제된 블록의 공백 제거
        const selectionAfterDelete = SelectionState.createEmpty(blockKey).merge({
            anchorOffset: 0,
            focusOffset: 1, // 공백 문자를 선택
        });

        const contentStateCleaned = Modifier.removeRange(
            contentStateWithoutBlock,
            selectionAfterDelete,
            'forward'
        );

        // 새로운 unstyled 블록으로 변환
        const newContentState = Modifier.setBlockType(
            contentStateCleaned,
            blockSelection,
            'unstyled'
        );

        const newEditorState = EditorState.push(
            editorState,
            newContentState,
            'remove-range'
        );

        setEditorState(newEditorState);
    };

    //  effect: 마운트시 실행할 함수 //
    useEffect(() => {
        const accessToken = cookies.accessToken;
        if (!accessToken) {
            navigator(AUTH_PATH());
            return;
        }
        if (!boardNumber) return;
        getBoardRequest(boardNumber).then(getBoardResponse);
        if (editorRef.current) {
            editorRef.current.focus();
        }
    }, [boardNumber]);

    //  render: 이미지 미리보기 컴포넌트 렌더링 //
    const cachedSrc: Record<string, string> = {}; // 블록 데이터 캐시
    const blockRendererFn = (contentBlock: ContentBlock) => {
        const blockKey = contentBlock.getKey();
        const contentState = editorState.getCurrentContent();
        const entityKey = contentBlock.getEntityAt(0);

        // 블록 타입이 'atomic'이면서 엔티티가 존재하는 경우 처리
        if (contentBlock.getType() === "atomic" && entityKey) {
            const entityData = contentState.getEntity(entityKey).getData();

            // 이미지 src가 변경되지 않았다면 렌더링하지 않음
            if (entityData.src === cachedSrc[blockKey]) {
                return null;
            }

            // 새로운 src 데이터 캐싱
            cachedSrc[blockKey] = entityData.src;

            return {
                component: (props: any) => (
                    <ImageBlock
                        src={entityData.src}
                        id={entityData.id}
                        onRemove={onImageCloseButtonClickHandler}
                    />
                ),
                editable: false,
            };
        }

        return null; // 다른 블록은 렌더링하지 않음
    };

    //  render: 게시물 수정 화면 컴포넌트 렌더링 //
    return (
        <div id='board-update-wrapper'>
            <div className='board-update-container'>
                <div className='board-update-box'>
                    <div className='board-update-title-box'>
                        <div className='category-select-box'>
                            <select className='category-select' value={category} onChange={onCategoryChangeHandler}>
                                <option value="">게시판을 선택해 주세요</option>
                                <option value="java">Java</option>
                                <option value="spring">Spring</option>
                                <option value="AWS">AWS</option>
                            </select>
                        </div>
                        <textarea ref={titleRef} className='board-update-title-textarea' rows={1} placeholder='제목을 작성해주세요.' value={title} onChange={onTitleChangeHandler}/>
                    </div>
                    <div className='divider'></div>
                    <div className='board-update-content-box'>
                        <div className={editorStyles.editor}>
                            <Editor
                                editorState={editorState}
                                onChange={onEditorChangeHandler}
                                keyBindingFn={keyBindingFn}
                                handleKeyCommand={handleKeyCommand}
                                blockRendererFn={blockRendererFn}
                                plugins={plugins}
                                customStyleMap={customStyleMap}
                            />
                            <Toolbar>
                                {(externalProps) => (
                                    <>
                                        <BoldButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <ItalicButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <UnderlineButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <CodeButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <Separator />
                                        <HeadlineOneButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <HeadlineTwoButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <HeadlineThreeButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <UnorderedListButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <OrderedListButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <BlockquoteButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <CodeBlockButton {...externalProps} getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <Separator />
                                        <ColorButton
                                            getEditorState={getEditorState}
                                            setEditorState={setEditorStateHandler}
                                        />
                                    </>
                                )}
                            </Toolbar>
                        </div>
                        <div className='icon-button' onClick={onImageUploadButtonClickHandler}>
                            <div className='icon image-box-light-icon'></div>
                        </div>
                        <input ref={imageInputRef} type='file' accept='image/*' style={{ display: 'none' }} onChange={onImageChangeHandler} />
                    </div>
                </div>
            </div>
        </div>
    )
};