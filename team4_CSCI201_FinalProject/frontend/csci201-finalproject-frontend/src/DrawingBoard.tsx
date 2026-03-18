import React, { useRef, useState, useEffect } from 'react';
// import { io } from 'socket.io-client';

// // Initialize the WebSocket connection
// const socket = io('ws://localhost:8080/game');

const DrawingBoard: React.FC = () => {
    const canvasRef = useRef<HTMLCanvasElement | null>(null);
    const [isDrawing, setIsDrawing] = useState(false);
    const [brushColor, setBrushColor] = useState('#000000');

    useEffect(() => {
        const canvas = canvasRef.current;
        if (canvas){
            canvas.width = 800; 
            canvas.height = 600;
            const ctx = canvas.getContext('2d');
            if(ctx) {
                ctx.fillStyle = '#ffffff'; //white canvas
                ctx.fillRect(0, 0, canvas.width, canvas.height);
            }
        }

        // socket.on('drawing', (data: { x: number; y: number; color: string }) => {
        //     const ctx = canvasRef.current?.getContext('2d');
        //     if (ctx) {
        //         ctx.strokeStyle = data.color;
        //         ctx.lineTo(data.x, data.y);
        //         ctx.stroke();
        //     }
        // });

        return () => {
            //socket.off('drawing');
        };
    }, []);

    const startDrawing = (e: React.MouseEvent<HTMLCanvasElement>) => {
        setIsDrawing(true);
        const ctx = canvasRef.current?.getContext('2d');
        if(ctx) {
            ctx.beginPath();
            ctx.moveTo(e.nativeEvent.offsetX, e.nativeEvent.offsetY);
        }
    };

    const draw = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!isDrawing) return;

        const ctx = canvasRef.current?.getContext('2d');
        if(ctx) {
            const x = e.nativeEvent.offsetX;
            const y = e.nativeEvent.offsetY;
            ctx.lineTo(x, y);
            ctx.strokeStyle = brushColor;
            ctx.lineWidth = 2; //brush size
            ctx.stroke();

            //socket.emit('drawing', { x, y, color: brushColor });
        }
    };

    const stopDrawing = () => {
        setIsDrawing(false);
        const ctx = canvasRef.current?.getContext('2d');
        if (ctx) ctx.closePath();
    };

    return (
        <div>
            <canvas
                ref={canvasRef}
                onMouseDown={startDrawing}
                onMouseMove={draw}
                onMouseUp={stopDrawing}
                onMouseLeave={stopDrawing}
                style={{ border: '1px solid black', cursor: 'crosshair' }}
            />
            <div>
                <label htmlFor="colorPicker">Brush Color:</label>
                <input
                    type="color"
                    id="colorPicker"
                    value={brushColor}
                    onChange={(e) => setBrushColor(e.target.value)}
                />
            </div>
        </div>
    );
};

export default DrawingBoard;



