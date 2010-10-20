<#import "spring.ftl" as spring >
<#assign portalName = portalName/>
<#if user??>
    <#assign user = user/>
</#if>
<#assign useCache = "false">
<#if RequestParameters.useCache??>
    <#assign useCache = "${RequestParameters.useCache}"/>
</#if>
<#-- used for grabbing locale based includes and images -->
<#-- locale also used on homepage for language based announcements/disclaimers -->
<#assign locale = springMacroRequestContext.locale>
<#assign query = "">
<#assign cacheUrl = cacheUrl>
<#assign view = "table">
<#if RequestParameters.view??>
    <#assign view = "${RequestParameters.view}">
</#if>
<#if RequestParameters.query??>
    <#assign query = "${RequestParameters.query}">
</#if>
<#if format??>
    <#assign format = format>
</#if>
<#assign interfaceLanguage = interfaceLanguage/>
<#macro localeBased type toSwitch>
    <#-- if locale is something like en_us, then shorten to en -->
    <#if locale?length &gt; 2>
        <#assign locale =  locale?substring(0, 2)>
    </#if>
    <#if type == "include">
        <#-- current available languages -->
        <#assign langs = ["ca","cs","da","de","el","en","es","et","fi","fr","ga","hu","is","it","lt","lv","mt","nl","no","pl","pt","sk","sl","sv"]>
    <#if toSwitch == "inc_usingeuropeana_$$.ftl">
    <#-- current available languages for this particular file -->
        <#assign langs = ["de","en","es","fr"]>
    </#if>
    <#if langs?seq_index_of(locale) != -1>
        <#include "${toSwitch?replace('$$', locale)}">
    <#else>
        <#include "${toSwitch?replace('$$','en')}">
    </#if>
    <#else>
        <img src="images/${toSwitch?replace('$$',locale)!'think_culture_en_small.gif'}" alt=""/>
    </#if>
</#macro>

<#-- shorten a given string and append with ellipses -->
<#macro stringLimiter theStr size>
    <#assign newStr = theStr>
    <#if newStr?length &gt; size?number>
    <#assign newStr = theStr?substring(0,size?number) + "...">
    </#if>
    ${newStr}
</#macro>

<#macro SearchForm className>

    <#assign showAdv="none"/>
    <#assign showSim="block"/>
    <#if pageId??>
        <#if pageId=="adv">
            <#assign showAdv="block"/>
            <#assign showSim="none"/>
        </#if>
    </#if>

    <div id="search_simple" class="${className}" style="display:${showSim};">
        <#if result?? >
            <#if result.badRequest?? >
                <span style="font-style: italic;">Wrong query. ${result.errorMessage}</span>
            </#if>
            <#--<#if result.spellCheck??>-->
               <#--<#if !result.spellCheck.correctlySpelled>did you mean: <a href="/${portalName}/brief-doc.html?query=${result.spellCheck.collatedResult}">${result.spellCheck.collatedResult}</a></#if>-->
            <#--</#if>-->
        </#if>
        <form method="get" action="/${portalName}/brief-doc.html" accept-charset="UTF-8" onsubmit="return checkFormSimpleSearch('query');">
            <input type="hidden" name="start" value="1" />
            <input type="hidden" name="view" value="${view}" />
            <input class="txt-input" name="query" id="query" type="text" title="Europeana Search" maxlength="75" />
            <button id="submit_search" type="submit" class="btn-search"><@spring.message 'Search_t' /></button>
            <br/>
            <a href="advancedsearch.html" id="href-advanced" title="<@spring.message 'AdvancedSearch_t' />"><@spring.message 'AdvancedSearch_t' /></a>
        </form>
    </div>

    <div id="search_advanced" class="${className}" style="display:${showAdv};" title="<@spring.message 'AdvancedSearch_t' />">
       <form method="get" action="/${portalName}/brief-doc.html" accept-charset="UTF-8">
        <input type="hidden" name="start" value="1" />
        <input type="hidden" name="view" value="${view}" />
        <table>
            <tr>
                <td>&#160;</td>
                <td><select name="facet1" id="facet1"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query1" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator2" id="operator2"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet2" id="facet2"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query2" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td align="right"><select name="operator3" id="operator3"><option value="and"><@spring.message 'AndBoolean_t'/> &nbsp;</option><option value="or"><@spring.message 'OrBoolean_t'/> </option><option value="not"><@spring.message 'NotBoolean_t'/> </option></select></td>
                <td><select name="facet3" id="facet3"><option value=""><@spring.message 'AnyField_t'/> &nbsp;</option><option value="title"><@spring.message 'Title_t'/></option><option value="creator"><@spring.message 'Creator_t'/></option><option value="date"><@spring.message 'Date_t'/></option><option value="subject"><@spring.message 'Subject_t'/></option></select></td>
                <td><input type="text" name="query3" class="search-input" maxlength="75"/></td>
            </tr>
            <tr>
                <td colspan="3">&#160;</td>
            </tr>
            <tr>
                <td align="left"><input type="reset" value="<@spring.message 'Reset_t' />" /></td>
                <td>&#160;</td>
                <td align="right"><input id="searchsubmit2" type="submit" value="<@spring.message 'Search_t' />" /></td>
            </tr>
         </table>
        </form>
    </div>
</#macro>

<#macro userbar>
<#include "language_select.ftl">
    <ul>
        <#if !user??>
            <li id="mustlogin"><a href="/${portalName}/login.html" onclick="takeMeBack();"><@spring.message 'LogIn_t'/></a></li>
            <li><a href="/${portalName}/register-request.html?pId=${pageId}"><@spring.message 'Register_t'/></a></li>
        </#if>

        <#if user??>
        <li>
            <@spring.message 'LoggedInAs_t' />: <strong>${user.userName?html}</strong> | <a
                href="/${portalName}/logout.html"><@spring.message 'LogOut_t' /></a>
        </li>
        <#if user.savedItems??>
        <li>
            <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '1', { expires: 1 });">
                <@spring.message 'SavedItems_t' />
            </a>
            (<span id="savedItemsCount">${user.savedItems?size}</span>)
        </li>
        </#if>
        <#if user.savedSearches??>
        <li>
            <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '2', { expires: 1 });">
                <@spring.message 'SavedSearches_t' />
            </a>
            (<span id="savedSearchesCount">${user.savedSearches?size}</span>)
        </li>
        </#if>
        <#if user.socialTags??>
        <li>
            <a href="/${portalName}/mine.html" onclick="$.cookie('ui-tabs-3', '3', { expires: 1 });">
                <@spring.message 'SavedTags_t' />
            </a>
            (<span id="savedTagsCount">${user.socialTags?size}</span>)
        </li>
        </#if>
        </#if>
    </ul>

</#macro>

<#macro admin>
    <#if user?? && (user.role == ('ROLE_ADMINISTRATOR') || user.role == ('ROLE_GOD'))>
    <div id="admin-block">
        <h4><@spring.message 'dms.administration.title' /></h4>

        <table class="user-options">
            <tbody>
                <tr>
                    <td><a href="/${portalName}/_.dml"><span class="ui-icon ui-icon-document"></span><@spring.message 'dms.administration.pages' /></a></td>
                </tr>
                <tr>
                    <td><a href="/${portalName}/_.img"><span class="ui-icon ui-icon-image"></span><@spring.message 'dms.administration.images' /></a></td>
                </tr>
                <tr>
                    <td><a href="/${portalName}/administration.html"><span class="ui-icon ui-icon-person"></span><@spring.message 'dms.administration.users' /></a></td>
                </tr>
            </tbody>
        </table>

    </div>
    </#if>
</#macro>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <#-- favicon_red.ico is also available -->
    <link rel="shortcut icon" href="/${portalName}/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/reset-text-grid.css"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/jquery-ui-1.8.5.custom.css"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/layout-common.css"/>

    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery-1.4.1.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery-ui-1.8.5.custom.min.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery.cookie.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery.toggleElements.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/jquery.validate.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/js_utilities.js"></script>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/results.js"></script>
    <script type="text/javascript">
        var msgRequired = "<@spring.message 'RequiredField_t'/>";
        var portalName = "/${portalName}";
        var baseThemePath = "/${portalName}/${portalTheme}";
    </script>
    <#switch thisPage>
    <#case "index.html">
    <#assign pageId = "in"/>
    <#assign bodyId = "home"/>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/index.js"></script>
    <title>Delving - Homepage</title>
    <#break>
    <#case "advancedsearch.html">
    <#assign pageId = "adv"/>
    <#assign bodyId = "advancedsearch"/>
    <#--<script type="text/javascript" src="js/lib/home.js"></script>-->
    <title>Delving - Advanced Search</title>
    <#break>
    <#case "brief-doc.html">
    <#assign pageId = "brd"/>
    <script type="text/javascript">
        var msgSearchSaveSuccess = "<@spring.message 'SearchSaved_t'/>";
        var msgSearchSaveFail = "<@spring.message 'SearchSavedFailed_t'/>";

        $(document).ready(function() {
            $('div.toggler-c').toggleElements(
            { fxAnimation:'slide', fxSpeed:'fast', className:'toggler' });
        });

    </script>
    <title>Delving - Search results</title>
    <#break>
    <#case "full-doc.html">
    <#assign pageId = "fd"/>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/fancybox/jquery.fancybox-1.3.1.pack.js"></script>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/fancybox/jquery.fancybox-1.3.1.css"/>
    <!--[if IE]>
    <style type="text/css">
        /* IE */
        #fancybox-loading.fancybox-ie div	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_loading.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-close		{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_close.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-title-over	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_over.png', sizingMethod='scale'); zoom: 1; }
        .fancybox-ie #fancybox-title-left	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_left.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-title-main	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_main.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-title-right	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_title_right.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-left-ico		{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_nav_left.png', sizingMethod='scale'); }
        .fancybox-ie #fancybox-right-ico	{ background: transparent; filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_nav_right.png', sizingMethod='scale'); }
        .fancybox-ie .fancy-bg { background: transparent !important; }
        .fancybox-ie #fancy-bg-n	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_n.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-ne	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_ne.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-e	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_e.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-se	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_se.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-s	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_s.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-sw	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_sw.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-w	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_w.png', sizingMethod='scale'); }
        .fancybox-ie #fancy-bg-nw	{ filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='/${portalName}/${portalTheme}/css/fancybox/fancy_shadow_nw.png', sizingMethod='scale'); }

    </style>
    <![endif]-->
    <#if user??>
    <script type="text/javascript">
        var msgItemSaveSuccess = "<@spring.message 'ItemSaved_t' />";
        var msgItemSaveFail = "<@spring.message 'ItemSaveFailed_t' />";
        var msgEmailSendSuccess = "<@spring.message 'EmailSent_t' />";
        var msgEmailSendFail = "<@spring.message 'EmailSendFailed_t' />";
        var msgEmailValid = "<@spring.message 'EnterValidEmail_t' />";
    </script>
    </#if>
    <title>Delving - Search results</title>
    <#break>
    <#case "mine.html">
    <#assign pageId = "me"/>
    <link rel="stylesheet" type="text/css" href="/${portalName}/${portalTheme}/css/mine.css"/>
    <script type="text/javascript" src="/${portalName}/${portalTheme}/js/mine.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#savedItems").tabs('select', $.cookie('ui-tabs-3'));
            $("#savedItems").tabs({ cookie: { expires: 30 } });
        });
    </script>
    <title>Delving - My Delving</title>
    <#break>
    <#case "exception.html">
    <title>Europeana - Exception</title>
    <#break>
    <#case "login.html">
    <#assign pageId = "li"/>
    <title>Delving - Login</title>
    <#break>
    <#case "logout.html">
    <#assign pageId = "lo"/>
    <title>Delving - Logout</title>
    <#break>
    <#case "register.html">
    <#assign pageId = "rg"/>
    <title>Delving - Registration</title>
    <#break>
    <#case "register-request.html">
    <#assign pageId = "rq"/>
    <title>Delving - Registration</title>
    <#break>
    <#case "forgot-password.html">
    <#assign pageId = "fp"/>
    <title>Delving - Forgot Password</title>
    <#break>
    <#case "change-password.html">
    <#assign pageId = "fp"/>
    <title>Delving - Forgot Password</title>
    <#break>
    <#case "register-success.html">
    <title>Delving - Registration continued</title>
    <#break>
    </#switch>

</head>

<body>
<@admin/>
<div id="container" class="container_12">