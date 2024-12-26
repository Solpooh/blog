import React from 'react';
import Editor from '@draft-js-plugins/editor';
import createToolbarPlugin from '@draft-js-plugins/static-toolbar';
import { EditorState } from 'draft-js';

// Toolbar 플러그인 생성
const toolbarPlugin = createToolbarPlugin();
const { Toolbar } = toolbarPlugin;

// MyEditor의 props 인터페이스 정의
interface MyEditorProps {
    editorState: EditorState;
    onChange: (editorState: EditorState) => void;
}

const MyEditor: React.FC<MyEditorProps> = ({ editorState, onChange }) => (
    <div>
        <Toolbar />
        <Editor
            editorState={editorState}
            onChange={onChange}
            plugins={[toolbarPlugin]}
        />
    </div>
);

export default MyEditor;
