// GameRoom.tsx
import React, { useState, useEffect } from 'react';
import { useWebSocket } from '../context/WebSocketContext';
import { useParams } from 'react-router-dom';
import './GameRoom.css';

interface Player {
  username: string;
}

const GameRoom: React.FC = () => {
  const { roomId } = useParams<{ roomId: string }>();
  const { sendMessage, registerHandler, unregisterHandler, isConnected } = useWebSocket();
  const [chatMessages, setChatMessages] = useState<string[]>([]);
  const [drawingData, setDrawingData] = useState<any[]>([]);
  const [currentDrawer, setCurrentDrawer] = useState<string | null>(null);
  const [players, setPlayers] = useState<Player[]>([]);

  useEffect(() => {
    const handleWebSocketMessage = (action: string, data: any) => {
      switch (action) {
        case 'chat':
          console.log("Chat message received:", data);
          setChatMessages((prev) => [...prev, data.message]);
          break;
        case 'draw':
          setDrawingData((prev) => [...prev, data.drawingData]);
          break;
        case 'drawer_chosen':
          setCurrentDrawer(data.username);
          break;
        case 'update_players':
          setPlayers(data.players);
          break;
        case 'message':
          console.log("Message from server:", data.message);
          // Optionally, display the error to the user
          break;
        case 'error':
          console.error("Error from server:", data.message);
          // Optionally, display the error to the user
          break;
        default:
          console.warn('Unhandled WebSocket action:', action);
      }
    };

    registerHandler(handleWebSocketMessage);

    return () => {
      unregisterHandler(handleWebSocketMessage);
    };
  }, [registerHandler, unregisterHandler]);

  const sendChatMessage = (message: string) => {
    if (!isConnected) {
      console.error('WebSocket is not connected.');
      return;
    }
    const username = sessionStorage.getItem('username');
    // const username = 'HardcodedUsername';
    if (!username) {
      console.error('Username not found in localStorage.');
      return;
    }
    
    sendMessage('chat', { username, message});
  };

  const handleSendMessage = () => {
    const input = document.getElementById('message-input') as HTMLInputElement;
    const message = input.value.trim();
    if (!message) return;

    sendChatMessage(message);
    input.value = '';
  };
  
  return (
    <div className="game-room">
      <div className="player-list">
        <h3>Players</h3>
        <ul>
          {players.map((player) => (
            <li key={player.username} style={{ fontWeight: player.username === currentDrawer ? 'bold' : 'normal' }}>
              {player.username} {player.username === currentDrawer && '(Drawer)'}
            </li>
          ))}
        </ul>
      </div>

      <div className="drawer-info">
        {currentDrawer ? <h2>Current Drawer: {currentDrawer}</h2> : <h2>No drawer selected yet</h2>}
      </div>

      {/* Chat Section */}
	  <div className="chatbody">
	    <div className="chatroom">
	      <div className="chat-header">Chatroom</div>
	      <div id="chat-messages" className="chat-messages">
	        {/* Messages will be inserted here */}
	        {chatMessages.map((msg, index) => (
	          <div key={index}>{msg}</div>
	        ))}
	      </div>

	      <div className="chat-input">
	        <input
	          type="text"
	          id="message-input"
	          placeholder="Enter Your Guess Here..."
	          onKeyDown={(e) => {
	            if (e.key === 'Enter') {
	              handleSendMessage();
	            }
	          }}
	        />
	        <button
	          id="send-button"
	          onClick={() => {
	            handleSendMessage();
	          }}
	        >
	          Send
	        </button>
	      </div>
	    </div>
	  </div>

      {/* Drawing Section */}
      <div className="drawing-board">
        <h3>Drawing Board</h3>
        <canvas
          id="drawingCanvas"
          width={800}
          height={600}
          // Drawing event handlers...
        ></canvas>
      </div>
    </div>
  );
};

export default GameRoom;
