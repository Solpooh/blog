import React from 'react';
import './style.css'

interface Props {
    src: string;
    id: string;
    onRemove: (id: string) => void;
}

const ImageBlock: React.FC<Props> = React.memo(({ src, id, onRemove }) => {
    return (
        <div className="board-write-image-box">
            <img src={src} className="board-write-image" alt="Uploaded" />
            <div
                className="icon-button image-close"
                onClick={() => onRemove(id)}
            >
                <div className="icon close-icon"></div>
            </div>
        </div>
    );
},(prevProps, nextProps) => prevProps.src === nextProps.src && prevProps.id === nextProps.id);

export default ImageBlock;