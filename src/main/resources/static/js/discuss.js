// html页面加载完成之后，调用此页面函数
$(function(){
    // 给页面上的按钮添加绑定事件，点这三个按钮调用三个不同的方法
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

// 发送异步请求
function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        // 访问路径
        CONTEXT_PATH + "/like",
        // 携带参数
        {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId":postId},

        // 处理返回数据
        function (data) {
            // 返回数据转化为Json对象
            data = $.parseJSON(data);

            if (data.code == 0) {
                // 请求成功
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : "赞");
            } else {
                // 请求失败
                alert(data.msg);
            }
        }
    );
}

// 置顶
function setTop() {
    // 发送异步请求传参数
    $.post(
        // URL：发送请求的地址
        CONTEXT_PATH + "/discuss/top",
        // data：发送给后台的数据，json格式
        // 携带的参数data：discuss-detail.html 里面的 id="postId" 的框的value
        {"id":$("#postId").val()},
        // callback：请求成功后的回调函数
        function(data) {
            // 将服务器返回的JSON格式数据转换为JavaScript对象，方便后续处理。
            data = $.parseJSON(data);
            if(data.code == 0) {
                // 如果返回数据的code属性为0，表示置顶成功
                // 此时将topBtn按钮设置为禁用状态，否则弹出错误信息。
                $("#topBtn").attr("disabled", "disabled");
            } else {
                // 弹出错误信息
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                // 跳转到首页
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}