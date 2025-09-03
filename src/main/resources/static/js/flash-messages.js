/**
 * 플래시 메시지 자동 숨김 기능
 */
document.addEventListener('DOMContentLoaded', function() {
    // 성공 메시지와 에러 메시지 선택
    const flashMessages = document.querySelectorAll('[class*="bg-green-"], [class*="bg-red-"]');
    
    flashMessages.forEach(function(message) {
        // 3초 후 페이드아웃 효과로 메시지 숨김
        setTimeout(function() {
            message.style.transition = 'opacity 0.5s ease-out';
            message.style.opacity = '0';
            
            // 페이드아웃이 완료되면 요소 제거
            setTimeout(function() {
                if (message.parentNode) {
                    message.parentNode.removeChild(message);
                }
            }, 500);
        }, 3000);
        
        // 클릭 시 즉시 닫기
        message.addEventListener('click', function() {
            message.style.transition = 'opacity 0.3s ease-out';
            message.style.opacity = '0';
            
            setTimeout(function() {
                if (message.parentNode) {
                    message.parentNode.removeChild(message);
                }
            }, 300);
        });
        
        // 닫기 버튼 추가 (선택사항)
        if (!message.querySelector('.close-btn')) {
            const closeBtn = document.createElement('button');
            closeBtn.className = 'close-btn float-right text-lg font-bold ml-4 hover:opacity-70';
            closeBtn.innerHTML = '&times;';
            closeBtn.setAttribute('aria-label', 'Close');
            
            closeBtn.addEventListener('click', function(e) {
                e.stopPropagation();
                message.style.transition = 'opacity 0.3s ease-out';
                message.style.opacity = '0';
                
                setTimeout(function() {
                    if (message.parentNode) {
                        message.parentNode.removeChild(message);
                    }
                }, 300);
            });
            
            message.appendChild(closeBtn);
        }
    });
});