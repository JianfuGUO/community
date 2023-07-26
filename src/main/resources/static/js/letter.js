$(function () {
    $("#sendBtn").click(send_letter);
    $(".close").click(delete_msg);
});

function send_letter() {
    $("#sendModal").modal("hide");

    // 从页面上根据标签id获取参数值
    var toName = $("#recipient-name").val();
    var content = $("#message-text").val();

    // 发送 ajax 异步请求
    $.post(
        // 地址
        CONTEXT_PATH + "/letter/send",

        // 向服务器提交的数据
        {"toName": toName, "content": content},

        // 回调函数，data 为服务器给浏览器返回的数据(字符串)
        function (data) {

            // jQuery将字符串转换为 json 对象
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#hintBody").text("发送成功!");
            } else {
                $("#hintBody").text(data.msg);
            }

            // 刷新页面，2秒后自动关闭
            $("#hintModal").modal("show");
            setTimeout(function () {
                $("#hintModal").modal("hide");
                location.reload();
            }, 2000);
        }
    );
}

function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}