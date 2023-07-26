$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    // 隐藏弹出框
    $("#publishModal").modal("hide");

    //发送AJAX请求之前,将CSRF令牌设置到请求的消息头中.
    // $里面是jQuery选择器来获取html上面的元素
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // // 发送请求之前对整个请求的参数进行设置
    // // function为页面函数（参数1，参数2，参数3）
    // // xhr为发送异步请求的核心对象
    // $(document).ajaxSend(function (e, xhr, options) {
    //     xhr.setRequestHeader(header, token);
    // });

    // 获取标题和内容
    // jQuery 表达式
    // 根据标签 id 来获取
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    // 发送AJAX 异步请求（post）
    $.post(
        // 请求路径
        CONTEXT_PATH + "/discuss/add",
        // 提交的数据
        {"title": title, "content": content},

        // 回调函数(处理返回的结果)
        function (data) {
            // 将返回的数据转换为 json 对象
            data = $.parseJSON(data);
            // 在提示框中显示返回消息
            $("#hintBody").text(data.msg);
            // 显示提示框
            $("#hintModal").modal("show");
            // 2秒后自动隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 成功后重新加载页面
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 2000);

        }
    );


}