import React, {useEffect, useState} from 'react';
import {RichUtils} from 'draft-js';
import {ColorMap} from 'types/enum';
import './style.css';

//  component: Color Button 컴포넌트 //
export const ColorButton = ({ getEditorState, setEditorState }: any) => {
    //  state: color 팝업 열림 여부 상태  //
    const [isExpanded, setIsExpanded] = useState(false);

    //  텍스트/배경 색상 추출
    const colorStyles = Object.keys(ColorMap)
        .filter(key => key.startsWith('CUSTOM_COLOR_')) as (keyof typeof ColorMap)[];
    const backgroundStyles = Object.keys(ColorMap)
        .filter(key => key.startsWith('CUSTOM_BACKGROUND_')) as (keyof typeof ColorMap)[];

    //  function: EditorState style 지정 함수 //
    const applyStyle = (style: string) => {
        const newState = RichUtils.toggleInlineStyle(getEditorState(), style);
        setEditorState(newState);
    };

    //  effect: 팝업 초기화 시 닫기 //
    useEffect(() => {
        setIsExpanded(false);
    }, []);


    return (
        <div className="color-button-container">
            {/* 색상 선택 버튼 */}
            <button className="color-toggle-button" onClick={() => setIsExpanded(!isExpanded)} />

            {/* 팝업: 색상 선택 */}
            {isExpanded && (
                <div className="color-popup">
                    {/* 텍스트 색상 섹션 */}
                    <div className="color-section">
                        <div className="color-section-title">텍스트 색상</div>
                        <div className="color-buttons">
                            {colorStyles.map(style => (
                                <button
                                    className="color-button"
                                    key={style}
                                    onClick={() => applyStyle(style)}
                                    style={{ backgroundColor: ColorMap[style] }}
                                ></button>
                            ))}
                        </div>
                    </div>

                    {/* 배경 색상 섹션 */}
                    <div className="color-section">
                        <div className="color-section-title">배경 색상</div>
                        <div className="color-buttons">
                            {backgroundStyles.map(style => (
                                <button
                                    className="color-button"
                                    key={style}
                                    onClick={() => applyStyle(style)}
                                    style={{ backgroundColor: ColorMap[style] }}
                                ></button>
                            ))}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};