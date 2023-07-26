$(function () {
    $(".follow-btn").click(follow);
});

function follow() {
    var btn = this;
    if ($(btn).hasClass("btn-info")) {
        // 关注TA
        // 异步请求
        $.post(
            // 访问路径
            CONTEXT_PATH + "/follow",
            // 携带的数据
            {"entityType": 3, "entityId": $(btn).prev().val()},
            // 处理返回值
            function (data) {
                // 关注它
                // 先将返回值转成JSON形式
                data = $.parseJSON(data);
                if (data.code == 0) {
                    // 前端页面更改样式
                    // 刷新页面
                    window.location.reload();
                } else {
                    // 弹出返回信息
                    alert(data.msg);
                }
            }
        );

        //  $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
    } else {
        // 取消关注
        // 异步请求
        $.post(
            // 访问路径
            CONTEXT_PATH + "/unfollow",
            // 携带的数据
            {"entityType": 3, "entityId": $(btn).prev().val()},
            // 处理返回值
            function (data) {
                // 关注它
                // 先将返回值转成JSON形式
                data = $.parseJSON(data);
                if (data.code == 0) {
                    // 前端页面更改样式
                    // 刷新页面
                    window.location.reload();
                } else {
                    // 弹出返回信息
                    alert(data.msg);
                }
            }
        );
        $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
    }
}