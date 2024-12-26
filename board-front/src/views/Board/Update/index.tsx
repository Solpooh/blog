import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {useBoardStore, useLoginUserStore} from 'stores';
import {MAIN_PATH} from '../../../constants';
import {useNavigate, useParams} from 'react-router-dom';
import {useCookies} from 'react-cookie';
import {getBoardRequest} from 'apis';
import {GetBoardResponseDto} from 'apis/response/board';
import {ResponseDto} from 'apis/response';
import {convertUrlsToFile} from 'utils';
import {ContentState, convertFromRaw, EditorState} from 'draft-js';
import Editor, {createEditorStateWithText} from '@draft-js-plugins/editor';
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
    const [imageUrls, setImageUrls] = useState<string[]>([]);

    const [editorState, setEditorState] = useState(EditorState.createEmpty());
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
        setTitle(title);
        setCategory(category);
        setImageUrls(boardImageList);
        convertUrlsToFile(boardImageList).then(boardImageFileList => setBoardImageFileList(boardImageFileList));

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
     //  event handler: 내용 변경 이벤트 처리 //
    // const onContentChangeHandler = (event: ChangeEvent<HTMLTextAreaElement>) => {
    //     const { value } = event.target;
    //     setContent(value);
    //
    //     if (!contentRef.current) return;
    //     contentRef.current.style.height = 'auto';
    //     contentRef.current.style.height = `${contentRef.current.scrollHeight}px`;
    // }

    //  event handler: editor 내용 변경 이벤트 처리  //
    const onEditorChangeHandler = (newState: EditorState) => {
        setEditorState(newState);

        // 순수 텍스트 변환
        const plainText = newState.getCurrentContent().getPlainText();
        setContent(plainText);
    }
    //  event handler: 이미지 변경 이벤트 처리 //
    const onImageChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
        if (!event.target.files || !event.target.files.length) return;
        const file = event.target.files[0];

        // 이미지 미리보기용 URL 만들기
        const imageUrl = URL.createObjectURL(file);
        const newImageUrls = imageUrls.map(item => item);
        newImageUrls.push(imageUrl);
        setImageUrls(newImageUrls);

        // 이미지 업로드용
        const newBoardImageFileList = boardImageFileList.map(item => item);
        newBoardImageFileList.push(file);
        setBoardImageFileList(newBoardImageFileList);

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
    const onImageCloseButtonClickHandler = (deleteIndex: number) => {
        if (!imageInputRef.current) return;
        imageInputRef.current.value = '';

        const newImageUrls = imageUrls.filter((url, index) => index !== deleteIndex);
        setImageUrls(newImageUrls);

        const newBoardImageFileList = boardImageFileList.filter((file, index) => index !== deleteIndex);
        setBoardImageFileList(newBoardImageFileList);
    }

    //  effect: 마운트시 실행할 함수 //
    useEffect(() => {
        const accessToken = cookies.accessToken;
        if (!accessToken) {
            navigator(MAIN_PATH());
            return;
        }
        if (!boardNumber) return;
        getBoardRequest(boardNumber).then(getBoardResponse);
        setEditorState(createEditorStateWithText(content));
    }, [boardNumber]);

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
                        {/*<textarea ref={contentRef} className='board-update-content-textarea' placeholder='내용을 작성해주세요.' value={content} onChange={onContentChangeHandler} />*/}
                        <div className={editorStyles.editor}>
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
                                    </>
                                )}
                            </Toolbar>
                            <Editor
                                editorState={editorState}
                                onChange={onEditorChangeHandler}
                                plugins={plugins}
                            />
                        </div>
                        <div className='icon-button' onClick={onImageUploadButtonClickHandler}>
                            <div className='icon image-box-light-icon'></div>
                        </div>
                        <input ref={imageInputRef} type='file' accept='image/*' style={{ display: 'none' }} onChange={onImageChangeHandler} />
                    </div>
                    <div className='board-update-images-box'>
                        {imageUrls.map((imageUrl, index) =>
                            <div key={index} className='board-update-image-box'>
                                <img className='board-update-image'src={imageUrl} />
                                <div className='icon-button image-close' onClick={() => onImageCloseButtonClickHandler(index)}>
                                    <div className='icon close-icon'></div>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
};