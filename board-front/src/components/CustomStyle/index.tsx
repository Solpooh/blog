import React from 'react';
import {RichUtils} from 'draft-js';

//  style: 사용자 정의 스타일 //
export const styleMap = {
    'CUSTOM_COLOR': { color: '#FF5733' },
    'CUSTOM_BACKGROUND': { backgroundColor: '#FFFF00'},
};

//  component: 텍스트 색상 버튼 컴포넌트 //
export const TextColorButton = ({ getEditorState, setEditorState }: any) => {
    const applyText = () => {
        const newState = RichUtils.toggleInlineStyle(getEditorState(), 'CUSTOM_COLOR');
        setEditorState(newState);
    };

    return <button onClick={applyText} style={{ margin: '0 5px' }}>Text Color</button>
};

//  component: 배경 색상 버튼 컴포넌트 //
export const BackgroundColorButton = ({ getEditorState, setEditorState }: any) => {
    const applyBackground = () => {
        const newState = RichUtils.toggleInlineStyle(getEditorState(), 'CUSTOM_BACKGROUND');
        setEditorState(newState);
    };

    return <button onClick={applyBackground} style={{ margin: '0 5px' }}>Background Color</button>
};