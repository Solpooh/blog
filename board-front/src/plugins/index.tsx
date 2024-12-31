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


// function: prismjs blockRendererFn 정의 //
// export const blockRendererFn = (contentBlock: { getType: () => any; }) => {
//     const type = contentBlock.getType();
//     if (type === "code-block") {
//         return {
//             component: (props: { block: any; contentState: any; }) => {
//                 const { block, contentState } = props;
//                 const text = block.getText();
//                 const language = block.getData().get("language") || "java"; // 기본 언어 설정
//                 useEffect(() => {
//                     Prism.highlightAll(); // Prism.js로 하이라이트 적용
//                 }, [text]);
//                 return (
//                     <pre className={`language-${language}`}>
//                 <code>{text}</code>
//                 </pre>
//             );
//             },
//             editable: true,
//         };
//     }
//     return null;
// };
