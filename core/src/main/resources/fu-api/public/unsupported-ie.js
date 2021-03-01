    var theUA = window.navigator.userAgent.toLowerCase();
    console.log(theUA);
    if ((theUA.match(/msie\s\d+/) && theUA.match(/msie\s\d+/)[0]) || (theUA.match(/trident[\/\s]?\d+/) && theUA.match(/trident[\/\s]?\d+/)[0])) {
        var str = "不支持IE浏览器,请更换您的浏览器";
        var str2 = "<h2 style='font-weight:900;padding:10px 0;'>推荐使用：<a href='https://www.baidu.com/s?ie=UTF-8&wd=%E8%B0%B7%E6%AD%8C%E6%B5%8F%E8%A7%88%E5%99%A8' target='_blank' style='color:#ffffff;'>谷歌</a>,"
            + "<a href='https://www.baidu.com/s?ie=UTF-8&wd=%E7%81%AB%E7%8B%90%E6%B5%8F%E8%A7%88%E5%99%A8' target='_blank' style='color:#ffffff;'>火狐</a>,"
            + "或其他双核极速模式</h2>";
        document.writeln("<pre style='font-size:25px;text-align:center;color:#fff;background-color:#0cc; height:100%;border:0;position:fixed;top:0;left:0;width:100%;z-index:1234'>" +
            "<h2 style='padding-top:200px;margin:0'><strong>" + str + "<br/></strong></h2><h2>" +
            str2 + "</h2><h2 style='margin:0'><strong>如果您的使用的是360、搜狗、QQ等双核浏览器，请在最顶部切换到极速模式访问<br/></strong></h2></pre>");
        document.execCommand("Stop");
    }