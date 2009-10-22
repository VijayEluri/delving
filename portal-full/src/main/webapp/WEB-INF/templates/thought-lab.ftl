<#import "/spring.ftl" as spring />
<#assign thisPage = "thought-lab.html"/>
<#include "inc_header.ftl"/>
<style>p {line-height:1.5em;}</style>
<div id="doc4" class="yui-t2">
    <div id="hd">
        <#include "inc_top_nav.ftl"/>
    </div>
    <div id="bd">
        <div id="yui-main">
            <div class="yui-b">
                <h1><@spring.message 'ThoughtLab_t' /></h1>

                <div class="yui-gb">
                    <div class="yui-u first" style="width:65%">
                        <p>Europeana.eu is a collaboration between universities, research institutes and
                            content providers. The site is a prototype, and in the coming year we will be
                            developing it in response to users' feedback and making it operational.
                        </p>

                        <p>
                            We will demonstrate new technologies and ideas here in Thought lab. Because these are
                            experiments, some of the technologies don't necessarily comply with accessibility standards
                            yet.
                        </p>
                    </div>
                    <div class="yui-u">
                        <img src="images/rodin-1.jpg" alt="Rodin image"/>
                    </div>
                </div>

            <div  class="yui-g">
                  <div  class="yui-u first">
                        <h2 style="margin: 0 0 10px 0;">Semantic Search Lab</h2>

                       <p>
                        A research prototype of a semantic search engine for <a href="http://www.europeana.eu">Europeana</a>.
                        As this is work in progress, multilingual support is still limited. The prototype's interface is currently
                        only available in English, French and Dutch. The search engine contains data of the <a href="http://www.rijksmuseum.nl" target="_blank">Rijksmuseum Amsterdam</a>,
                        the <a href="http://www.louvre.fr">Mus&#233;e du Louvre</a> in Paris, and the <a href="http://www.rkd.nl" target="_blank">Rijksbureau voor Kunsthistorische Documentatie
                        (Netherlands Institute for Art History).</a> in The Hague. These data are for demonstration purposes only; some data might be outdated and there
                        is no claim of completeness..
                        </p>
                        <p>
                            <a href="http://eculture.cs.vu.nl/europeana/session/search" target="_blank">see the prototype</a>
                        </p>
                  </div>
                  <div  class="yui-u">
                        <a href="http://eculture.cs.vu.nl/europeana/session/search" target="_blank">
                            <img src="images/semanticSearch.jpg" alt="" style="margin: 40px"/>
                        </a>
                  </div>
            </div>

                <#--<a href="javascript:PicLensLite.start();">Insert Content Here</a>-->

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