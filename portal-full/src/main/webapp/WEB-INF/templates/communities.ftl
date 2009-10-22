<#import "/spring.ftl" as spring />
<#assign thisPage = "communities.html"/>
<#include "inc_header.ftl"/>
<script>
    var demWin
    function showDemo(){

        if(!demWin || demWin.closed) {
            demWin = window.open('communities.html?page=list','demowindow','width=800,height=700,resizable=yes,toolbar=no,location=no,directories=no,menubar=no');
        }
        else {
            demWin.focus();
        }

    }
</script>
<body class=" yui-skin-sam">
<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
   <div id="bd">
    <div id="yui-main">
        <div class="yui-b">
            <h1>Communities</h1>
            <h3>Welcome to the communities pages of Europeana!</h3>
            <p>We are looking at ideas for sharing, discussing, reusing and blogging about our content.</p>
            <p>Consider all of this as an open window to new ideas and your suggestions.</p>
            <p>Here is a demo with some of our thoughts.</p>
            <p>Click on the <strong><span style="color:orange;">orange</span> button</strong> and click on the <strong><span style="color:orange;">orange</span></strong> items to <strong>continue</strong>.</p>
            <p><button  class="button" style="background: orange; color: white; border: 1px solid #000;"
                onclick="showDemo();">Demo</button></p>

        </div>

   </div>
        <div class="yui-b">
            <#include "inc_logo_sidebar.ftl"/>
        </div>
    </div>
   <div id="ft">
	   <#include "inc_footer.ftl"/>
   </div>

</div>


</body>
</html>
