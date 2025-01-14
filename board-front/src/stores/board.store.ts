import {create} from 'zustand';
import {BoardImageFile} from 'types/interface';
import React from 'react';

interface BoardStore {
    title: string;
    content: string;
    category: string;
    boardImageFileList: BoardImageFile[];
    setTitle: (title: string) => void;
    setContent: (content: string) => void;
    setCategory: (category: string) => void;
    setBoardImageFileList: (boardImageFileList: BoardImageFile[]) => void;
    resetBoard: () => void;
    imageInputRef: React.RefObject<HTMLInputElement>;
}

const useBoardStore = create<BoardStore>(set => ({
    title: '',
    content: '',
    category: '',
    boardImageFileList: [],
    setTitle: (title) => set(state => ({ ...state, title })),
    setContent: (content) => set(state => ({ ...state, content })),
    setCategory: (category) => set(state => ({ ...state, category })),
    setBoardImageFileList: (boardImageFileList) => set(state => ({ ...state, boardImageFileList })),
    resetBoard: () => set(state => ({ ...state, title: '', content: '', boardImageFileList: []})),
    imageInputRef: React.createRef<HTMLInputElement>(),
}));

export default useBoardStore;