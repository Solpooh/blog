import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {useBoardStore, useEditorStore} from '../../../stores';
import {AUTH_PATH} from '../../../constants';
import {useNavigate} from 'react-router-dom';
import {useCookies} from 'react-cookie';
import {
    AtomicBlockUtils, ContentBlock,
    convertToRaw,
    EditorState,
    getDefaultKeyBinding,
    Modifier,
    RichUtils
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

//  component: 게시물 작성 화면 컴포넌트 //
export default function BoardWrite() {
    //  state: 제목 영역 요소 참조 상태 //
    const titleRef = useRef<HTMLTextAreaElement | null>(null);
    //  state: 이미지 입력 요소 참조 상태 //
    const { imageInputRef } = useBoardStore();
    //  state: 게시물 상태 //
    const { title, setTitle } = useBoardStore();
    const { content, setContent } = useBoardStore();
    const { category, setCategory } = useBoardStore();
    const { boardImageFileList, setBoardImageFileList } = useBoardStore();
    const { resetBoard } = useBoardStore();

    //  state: EditorState 상태 //
    const { editorState, setEditorState, resetEditorState } = useEditorStore();

    //  state: 쿠키 상태 //
    const [cookies, setCookies] = useCookies();

    //  state: 게시물 이미지 미리보기 URL 상태 //
    const [imageUrls, setImageUrls] = useState<ImageUrl[]>([]);

    const editorRef = useRef<Editor | null>(null);

    //  function: EditorState 접근 함수 //
    const getEditorState = () => editorState;
    const setEditorStateHandler = (newState: EditorState) => setEditorState(newState);

    //  function: 네비게이트 함수 //
    const navigator = useNavigate();

    //  function: CodeBlock에서 'Enter' 및 'Tab' 키 처리 함수 //
    const handleKeyCommand = (command: string): 'handled' | 'not-handled' => {
        const blockType = RichUtils.getCurrentBlockType(editorState);
        const currentContent = editorState.getCurrentContent();
        const selection = editorState.getSelection();

        // Code Block에서 Enter 및 Tab 처리
        if (blockType === 'code-block') {
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
    //  event handler: 제목 변경 이벤트 처리  //
    const onTitleChangeHandler = (event: ChangeEvent<HTMLTextAreaElement>) => {
        const { value } = event.target;
        setTitle(value);
        if (!titleRef.current) return;
        titleRef.current.style.height = 'auto';
        titleRef.current.style.height = `${titleRef.current.scrollHeight}px`;
    }

    //  event handler: editor 내용 변경 이벤트 처리  //
    const onEditorChangeHandler = (state: EditorState) => {
        setEditorState(state);

        const rawContent = JSON.stringify(convertToRaw(state.getCurrentContent()));
        setContent(rawContent);

        // console.log(editorState.getCurrentContent().getPlainText());
    };

    //  event handler: 이미지 변경 이벤트 처리  //
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

            // 기존 contentState를 가져오고 Entity 생성
            const contentStateWithEntity = newEditorState
                .getCurrentContent()
                .createEntity('IMAGE', 'IMMUTABLE', { src: imageUrl, id: id });

            const entityKey = contentStateWithEntity.getLastCreatedEntityKey();

            // 새로운 ContentState를 EditorState에 반영
            const updatedEditorState = EditorState.set(newEditorState, {
                currentContent: contentStateWithEntity,
            });

            setEditorState(
                AtomicBlockUtils.insertAtomicBlock(updatedEditorState, entityKey, ' '),
            )
        });

        setImageUrls(newImageUrls);
        setBoardImageFileList(newBoardImageFileList);
        // setEditorState(newEditorState);

        if (!imageInputRef.current) return;
        // 똑같은 이미지 새로운 등록을 위해 초기화
        imageInputRef.current.value = '';
    }
    //  event handler: 이미지 업로드 버튼 클릭 이벤트 처리 //
    const onImageUploadButtonClickHandler = () => {
        if (!imageInputRef.current) return;
        imageInputRef.current.click();
    }

    //  effect: 마운트시 실행할 함수 //
    useEffect(() => {
        const accessToken = cookies.accessToken;
        if (!accessToken) {
            navigator(AUTH_PATH());
            return;
        }
        if (editorRef.current) {
            editorRef.current.focus();
        }
        resetBoard();
        resetEditorState();
    }, []);

    //  render: 이미지 미리보기 컴포넌트 렌더링 //
    const blockRendererFn = (contentBlock: ContentBlock) => {
        if (contentBlock.getType() === 'atomic') {
            const contentState = editorState.getCurrentContent();
            const entity = contentBlock.getEntityAt(0);

            if (!entity) return null;

            const type = contentState.getEntity(entity).getType();
            if (type === 'IMAGE') {
                return {
                    component: ImageBlock,
                    editable: false,
                }
            }
        }
        return null;
    }

    const blockStyleFn = (contentBlock: any) => {
        if (contentBlock.getType() === "code-block") {
            return "custom-code-block";
        }
        return "";
    };

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
                                <option value="javascript">JavaScript</option>
                                <option value="sql">SQL</option>
                                <option value="spring">Spring</option>
                                <option value="spring-boot">Spring Boot</option>
                                <option value="spring-security">Spring Security</option>
                                <option value="mybatis">MyBatis</option>
                                <option value="jpa">JPA</option>
                                <option value="git">Git</option>
                                <option value="AWS">AWS</option>
                                <option value="computer-science">Computer Science</option>
                                <option value="network">Network</option>
                                <option value="react">React</option>
                                <option value="문제해결">문제해결</option>
                                <option value="동기부여">동기부여</option>
                                <option value="알고리즘">알고리즘</option>
                                <option value="포트폴리오">포트폴리오</option>
                            </select>
                        </div>
                        <textarea ref={titleRef} className='board-write-title-textarea' rows={1} placeholder='제목을 작성해주세요.' value={title} onChange={onTitleChangeHandler}/>
                    </div>
                    <div className='divider'></div>
                    <div className='board-write-content-box'>
                        <div className={editorStyles.editor}>
                            {/* 🛠️ 툴바를 고정하는 컨테이너 */}
                            <div className={editorStyles.toolbarContainer}>
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
                                            <ColorButton getEditorState={getEditorState} setEditorState={setEditorStateHandler} />
                                        </>
                                    )}
                                </Toolbar>
                            </div>

                            {/* ✍️ 에디터 영역 */}
                            <Editor
                                editorState={editorState}
                                onChange={onEditorChangeHandler}
                                keyBindingFn={keyBindingFn}
                                handleKeyCommand={handleKeyCommand}
                                blockRendererFn={blockRendererFn}
                                blockStyleFn={blockStyleFn}
                                plugins={plugins}
                                customStyleMap={customStyleMap}
                            />
                        </div>
                        <div className='icon-button' onClick={onImageUploadButtonClickHandler}>
                            <div className='icon image-box-light-icon'></div>
                        </div>
                        <input ref={imageInputRef} type='file' accept='image/*' style={{ display: 'none' }} onChange={onImageChangeHandler} multiple />
                    </div>
                </div>
            </div>
        </div>
    )
};