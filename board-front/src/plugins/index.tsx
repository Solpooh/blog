import {ColorMap} from 'types/enum';
import 'prismjs/themes/prism.css';

//  function: color 추출 함수 //
export const customStyleMap = Object.keys(ColorMap).reduce((map, key) => {
    const value = ColorMap[key as keyof typeof ColorMap];
    if (key.startsWith("CUSTOM_COLOR_")) {
        map[key] = { color: value }; // 텍스트 색상 설정
    } else if (key.startsWith("CUSTOM_BACKGROUND_")) {
        map[key] = { backgroundColor: value }; // 배경색 설정
    }
    return map;
}, {} as Record<string, { color?: string; backgroundColor?: string }>);