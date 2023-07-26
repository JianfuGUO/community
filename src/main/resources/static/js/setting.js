$(function () {
    // 给页面上的按钮绑定事件
    $("#uploadForm").submit(upload);
});

// 发送异步请求
function upload() {
    // $.post为简化参数的方式发送异步请求, $.ajax是比较完善的
    $.ajax({
        // ajax请求提交的路径
        url: "http://upload.qiniup.com",  //z1为华东地区
        method: "post", // 请求方式
        processData: false, // 不要把表单内容转换为字符串（本次上传的为图片）
        contentType: false, // 传递的数据类型，不让jQuery设置上传类型
        data: new FormData($("#uploadForm")[0]), // 将表单数据封装为FormData对象，并作为请求数据发送。
        success: function (data) { // 当请求成功时执行的回调函数，传入的参数data是服务器返回的响应数据。
            if (data && data.code == 0) {
                // 更新头像访问路径（UserController里面）--- 使用POST方法发送一个请求，将文件名作为请求参数发送给服务器。
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName": $("input[name='key']").val()},
                    function (data) {
                        // 普通字符串转换为JSON格式字符串
                        data = $.parseJSON(data);
                        if (data.code == 0) {
                            // 刷新当前页面
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败!");
            }
        }
    });

    // 不用提交表单，上面的逻辑已经将逻辑进行处理了
    return false;
}
