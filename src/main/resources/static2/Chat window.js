$(document).ready(function() {
    let Message;

    Message = function(arg) {
        this.text = arg.text;
        this.message_side = arg.message_side;
        this.draw = function(_this) {
            return function() {
                var message;
                message = $($('.message_template').clone().html());
                message.addClass(_this.message_side).find('.text').html(_this.text);
                $('.messages').append(message);
                return setTimeout(function() {
                    return message.addClass('appeared');
                }, 0);
            };
        }(this);
        return this;
    };

    let message_side = 'right';

    function getMessageText() {
        var message_input;
        message_input = $('.message_input');
        return message_input.val().trim();
    };

    function sendMessage(text) {
        if (text === '') {
            return;
        }
        let messages, message;
        messages = $('.messages');
        message_side = message_side === 'left' ? 'right' : 'left';
        message = new Message({
            text: text,
            message_side: message_side
        });
        message.draw();
        $('.message_input').val('');
        return messages.animate({ scrollTop: messages.prop('scrollHeight') }, 300);
    };

    // sendMessage('Hi Aditi!');
    // setTimeout(function () {
    //     return sendMessage('Hi Pooja');
    // }, 1000);
    // return setTimeout(function () {
    //     return sendMessage('Hi everyone!');
    // }, 2000);



    // *********************** Event Listeners *******************************

    $('.send_message').click(function(e) {
        return sendMessage(getMessageText());
    });
    $('.message_input').keyup(function(e) {
        if (e.which === 13) {
            console.log(getMessageText);
            return sendMessage(getMessageText());
        }
    });
});