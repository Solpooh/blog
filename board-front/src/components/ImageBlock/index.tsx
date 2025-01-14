import React, {useState} from 'react';
import './style.css'
import {ContentState, EditorState, Modifier, SelectionState} from 'draft-js';
import {useBoardStore, useEditorStore} from 'stores';
import {ImageUrl} from 'types/interface';

interface Props {
    block: any;
    contentState: ContentState
}

const ImageBlock: React.FC<Props> = ({ block, contentState }) => {
    const { imageInputRef } = useBoardStore();
    const { boardImageFileList, setBoardImageFileList } = useBoardStore();
    const { editorState, setEditorState } = useEditorStore();
    const [imageUrls, setImageUrls] = useState<ImageUrl[]>([]);

    const { src, id } = contentState.getEntity(block.getEntityAt(0)).getData();

    //  event handler: 이미지 닫기 버튼 클릭 이벤트 처리 //
    const onImageCloseButtonClickHandler = (deleteId: string) => {
        if (!imageInputRef.current) return;

        // 1. 이미지 인풋 초기화
        imageInputRef.current.value = '';
        // 2. 이미지 URL 상태 업데이트
        const newImageUrls = imageUrls.filter((image) => image.id !== deleteId);
        setImageUrls(newImageUrls);

        // 3. 업로드 파일 리스트 업데이트
        const newBoardImageFileList = boardImageFileList.filter((file) => file.id !== deleteId);
        setBoardImageFileList(newBoardImageFileList);

        // 4. 에디터 블록 삭제
        const contentState = editorState.getCurrentContent();
        const blockMap = contentState.getBlockMap();

        const blockToDelete = blockMap.find((block: any) => {
            const entityKey = block.getEntityAt(0);
            if (entityKey) {
                const entityData = contentState.getEntity(entityKey).getData();
                return entityData?.id === deleteId;
            }
            return false;
        });

        if (!blockToDelete) {
            console.warn('Block not found for id:', deleteId);
            return;
        }

        const blockKey = blockToDelete.getKey();

        // 커서 위치 지정
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

    return (
        <div className="board-write-image-box">
            <img src={src} className="board-write-image" alt="Uploaded" />
            <div className="icon-button image-close" onClick={() => onImageCloseButtonClickHandler(id)}>
                <div className="icon close-icon"></div>
            </div>
        </div>
    );
};

export default ImageBlock;