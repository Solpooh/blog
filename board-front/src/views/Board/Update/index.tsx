import React, {ChangeEvent, useEffect, useRef, useState} from 'react';
import './style.css';
import {useBoardStore, useEditorStore, useLoginUserStore} from 'stores';
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
    const { imageInputRef } = useBoardStore();

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

    //  state: EditorState 상태 //
    const { editorState, setEditorState } = useEditorStore();

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

    const blockCache = useRef(new Map<string, any>());

    //  effect: editorState가 변경되면 캐시 초기화 //
    useEffect(() => {
        blockCache.current.clear();
    }, [editorState]);

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