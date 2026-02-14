import {create} from 'zustand';
import {BoardImageFile} from 'types/interface';
import React from 'react';

interface BoardStore {
    title: string;
    content: string;
    category: string;
    boardImageFileList: BoardImageFile[];
    allowNavigation: boolean;
    setTitle: (title: string) => void;
    setContent: (content: string) => void;
    setCategory: (category: string) => void;
    setBoardImageFileList: (boardImageFileList: BoardImageFile[]) => void;
    setAllowNavigation: (allow: boolean) => void;
    resetBoard: () => void;
    imageInputRef: React.RefObject<HTMLInputElement>;
}

const useBoardStore = create<BoardStore>(set => ({
    title: '',
    content: '',
    category: '',
    boardImageFileList: [],
    allowNavigation: false,
    setTitle: (title) => set(state => ({ ...state, title })),
    setContent: (content) => set(state => ({ ...state, content })),
    setCategory: (category) => set(state => ({ ...state, category })),
    setBoardImageFileList: (boardImageFileList) => set(state => ({ ...state, boardImageFileList })),
    setAllowNavigation: (allow) => set(state => ({ ...state, allowNavigation: allow })),
    resetBoard: () => set(state => ({ ...state, title: '', content: '', boardImageFileList: [], allowNavigation: false })),
    imageInputRef: React.createRef<HTMLInputElement>(),
}));

export default useBoardStore;