import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {useBoardStore} from '../../../stores';
import {MAIN_PATH} from '../../../constants';
import {useNavigate} from 'react-router-dom';
import {useCookies} from 'react-cookie';
import {convertToRaw, EditorState} from 'draft-js';
import Editor from '@draft-js-plugins/editor';
import createInlineToolbarPlugin, {
    Separator,
} from '@draft-js-plugins/inline-toolbar';
import '@draft-js-plugins/inline-toolbar/lib/plugin.css';
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
import {BackgroundColorButton, TextColorButton, styleMap} from '../../../components/CustomStyle';

//  플러그인 설정
const inlineToolbarPlugin = createInlineToolbarPlugin();
const { InlineToolbar } = inlineToolbarPlugin;
const plugins = [inlineToolbarPlugin];

//  component: 게시물 작성 화면 컴포넌트 //
export default function BoardWrite() {

    //  state: 제목 영역 요소 참조 상태 //
    const titleRef = useRef<HTMLTextAreaElement | null>(null);
    //  state: 이미지 입력 요소 참조 상태 //
    const imageInputRef = useRef<HTMLInputElement | null>(null);

    //  state: 게시물 상태 //
    const { title, setTitle } = useBoardStore();
    const { content, setContent } = useBoardStore();
    const { category, setCategory } = useBoardStore();
    const { boardImageFileList, setBoardImageFileList } = useBoardStore();
    const { resetBoard } = useBoardStore();

    //  state: 쿠키 상태 //
    const [cookies, setCookies] = useCookies();

    //  state: 게시물 이미지 미리보기 URL 상태 //
    const [imageUrls, setImageUrls] = useState<string[]>([]);

    //  state: Editor State 상태 //
    const [editorState, setEditorState] = useState<EditorState>(EditorState.createEmpty());
    const getEditorState = () => editorState;
    const setEditorStateHandler = (newState: EditorState) => setEditorState(newState);

    //  function: 네비게이트 함수 //
    const navigator = useNavigate();

    //  event handler: 카테고리 변경 이벤트 처리  //
    const onCategoryChangeHandler = (event: ChangeEvent<HTMLSelectElement>) => {
        const { value } = event.target;
        setCategory(value);
    }
    //  event handler: 제목 변경 이벤트 처리  //
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

    //  event handler: 이미지 변경 이벤트 처리  //
    const onImageChangeHandler = (event: ChangeEvent<HTMLInputElement>) => {
        if (!event.target.files || !event.target.files.length) return;
        const file = event.target.files[0];

        // 이미지 미리보기용 URL 만들기 (blob -> url)
        const imageUrl = URL.createObjectURL(file);
        console.log(imageUrl); // localhost:3000
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
        resetBoard();
    }, []);

    //  render: 게시물 작성 화면 컴포넌트 렌더링 //
    return (
        <div id='board-write-wrapper'>
            <div className='board-write-container'>
                <div className='board-write-box'>
                    <div className='board-write-title-box'>
                        <div className='category-select-box'>
                            <select className='category-select' value={category} onChange={onCategoryChangeHandler}>
                                <option value="">게시판을 선택해 주세요</option>
                                <option value="java">Java</option>
                                <option value="spring">Spring</option>
                                <option value="AWS">AWS</option>
                            </select>
                        </div>
                        <textarea ref={titleRef} className='board-write-title-textarea' rows={1} placeholder='제목을 작성해주세요.' value={title} onChange={onTitleChangeHandler}/>
                    </div>
                    <div className='divider'></div>
                    <div className='board-write-content-box'>
                        {/*<textarea ref={contentRef} className='board-write-content-textarea' placeholder='내용을 작성해주세요.' value={content} onChange={onContentChangeHandler} />*/}
                        <div className={editorStyles.editor}>
                            <InlineToolbar>
                                {(externalProps) => (
                                    <>
                                        <BoldButton {...externalProps} />
                                        <ItalicButton {...externalProps} />
                                        <UnderlineButton {...externalProps} />
                                        <CodeButton {...externalProps} />
                                        <Separator />
                                        <HeadlineOneButton {...externalProps} />
                                        <HeadlineTwoButton {...externalProps} />
                                        <HeadlineThreeButton {...externalProps} />
                                        <UnorderedListButton {...externalProps} />
                                        <OrderedListButton {...externalProps} />
                                        <BlockquoteButton {...externalProps} />
                                        <CodeBlockButton {...externalProps} />
                                        <Separator />
                                        <TextColorButton getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        <BackgroundColorButton getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                    </>
                                )}
                            </InlineToolbar>
                            <Editor
                                editorState={editorState}
                                onChange={onEditorChangeHandler}
                                plugins={plugins}
                                customStyleMap={styleMap}
                            />
                        </div>
                        <div className='icon-button' onClick={onImageUploadButtonClickHandler}>
                            <div className='icon image-box-light-icon'></div>
                        </div>
                        <input ref={imageInputRef} type='file' accept='image/*' style={{ display: 'none' }} onChange={onImageChangeHandler} />
                    </div>
                    <div className='board-write-images-box'>
                        {imageUrls.map((imageUrl, index) =>
                            <div key={index} className='board-write-image-box'>
                                <img className='board-write-image' src={imageUrl} />
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