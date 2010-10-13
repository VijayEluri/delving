<#import "spring.ftl" as spring />
<#include "spring_form_macros.ftl"/>
<#assign thisPage = "register-request.html"/>

<#include "inc_header.ftl">

<div id="main">

    <div id="register-div" class="grid_4 login-register">

        <h2><@spring.message 'Register_t' /></h2>

        <form id="registrationForm" name='registrationForm' action='register-request.html' method='POST' accept-charset="UTF-8">
            <input type="hidden" name="formType" id="formType" value="Register"/>
            <table>
                <tr>
                    <td width="100"><label for="email"><@spring.message 'EmailAddress_t' /></label></td>
                    <td><input id="register_email" type='text' name='email' id="email" value='' accept-charset="UTF-8"></td>
                </tr>
                <tr>
                    <td></td>
                    <td><input id="register" name="submit_button" type="submit" value="<@spring.message 'Register_t' />" class="button"/></td>
                </tr>
            </table>
        </form>
        
        <#if state == "success">
            <p id="success" class="success">
                <@spring.message 'AnEmailHasBeenSentTo_t' />: <span class="fg-gold">${email}</span>.
                <@spring.message 'PleaseFollowTheLinkProvided_t' />.
            </p>
        </#if>
        <#if state == "formatFailure">
            <div class="ui-widget">
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong><@spring.message 'EmailFormatError_t' />.
                </div>
            </div>
        </#if>
        <#if state == "existsFailure">
            <div class="ui-widget">
                <div class="ui-state-error ui-corner-all" style="padding: 0pt 0.7em;">
                    <span class="ui-icon ui-icon-alert" style="float: left; margin-right: 0.3em;"></span>
                    <strong><@spring.message 'Error_t' />: </strong><@spring.message 'EmailAlreadyRegistered_t' />.
                </div>
            </div>
        </#if>

    </div>

    <div class="clear"></div>

<#include "inc_footer.ftl"/>

