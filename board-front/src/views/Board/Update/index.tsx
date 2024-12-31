import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {useBoardStore, useLoginUserStore} from 'stores';
import {AUTH_PATH, MAIN_PATH} from '../../../constants';
import {useNavigate, useParams} from 'react-router-dom';
import {useCookies} from 'react-cookie';
import {getBoardRequest} from 'apis';
import {GetBoardResponseDto} from 'apis/response/board';
import {ResponseDto} from 'apis/response';
import {convertUrlsToFile} from 'utils';
import {convertFromRaw, convertToRaw, EditorState, Modifier, RichUtils} from 'draft-js';
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
import {ColorButton} from '../../../components/ColorButton';
import {customStyleMap} from '../../../plugins';

//  플러그인 설정
const inlineToolbarPlugin = createInlineToolbarPlugin();
const { InlineToolbar } = inlineToolbarPlugin;
const plugins = [inlineToolbarPlugin];

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

    //  state: Editor State 상태 //
    const [editorState, setEditorState] = useState(EditorState.createEmpty());

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

    //  function: handleReturn => 엔터키, 탭키 동작 제어 함수 //
    const handleReturnOrTab = (event: { key: string }) => {
        const currentContent = editorState.getCurrentContent();
        const selection = editorState.getSelection();
        const blockType = RichUtils.getCurrentBlockType(editorState);

        if (blockType === 'code-block') {
            if (!selection.isCollapsed()) {
                return 'not-handled'; // 선택 영역이 비어있지 않은 경우 무시
            }

            if (event.key === 'Enter') {
                const newContent = Modifier.insertText(currentContent, selection, '\n');
                setEditorState(EditorState.push(editorState, newContent, 'insert-characters'));
                return 'handled';
            } else if (event.key === 'Tab') {
                const newContent = Modifier.replaceText(currentContent, selection, '    ');
                setEditorState(EditorState.push(editorState, newContent, 'insert-characters'));
                return 'handled';
            }
        }
        return 'not-handled';
    };

    // function: Tab 키를 감지하는 함수 //
    const keyBindingFn = (event: { key: string; }) => {
        if (event.key === 'Tab') {
            return 'tab'; // 커스텀 명령 반환
        }
        if (event.key === 'Enter') {
            return 'enter'; // Enter 키 커스텀 명령 반환
        }
    };

    //  function: 커스텀 명령 처리 //
    const handleKeyCommand = (command: string) => {
        if (command === 'tab' || command === 'enter') {
            return handleReturnOrTab({ key: command === 'tab' ? 'Tab' : 'Enter' });
        }
        return 'not-handled';
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
            navigator(AUTH_PATH());
            return;
        }
        if (!boardNumber) return;
        getBoardRequest(boardNumber).then(getBoardResponse);
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
                        <div className={editorStyles.editor}>
                            <Editor
                                editorState={editorState}
                                onChange={onEditorChangeHandler}
                                keyBindingFn={keyBindingFn}
                                handleKeyCommand={handleKeyCommand}
                                plugins={plugins}
                                customStyleMap={customStyleMap}
                            />
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
                                        <ColorButton
                                            getEditorState={getEditorState}
                                            setEditorState={setEditorStateHandler}
                                        />
                                    </>
                                )}
                            </InlineToolbar>
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