import { create } from 'zustand';
import { EditorState } from 'draft-js';

interface EditorStore {
    editorState: EditorState;
    setEditorState: (editorState: EditorState) => void;
    resetEditorState: () => void;
}

const useEditorStore = create<EditorStore>((set) => ({
    editorState: EditorState.createEmpty(),
    setEditorState: (editorState) => set(() => ({ editorState })),
    resetEditorState: () => set(() => ({ editorState: EditorState.createEmpty() })),
}));

export default useEditorStore;