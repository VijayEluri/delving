package eu.delving.core.binding

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import scala.collection.JavaConversions._

/**
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since 1/2/11 1:12 PM  
 */

class FreemarkerUtilSpec extends Spec with ShouldMatchers {
  
  describe("A FreemarkerUtil") {
      
      describe("(when given a range parameters)") {
        
        val params = asJavaMap(Map("query" -> Array("sjoerd"), "qf" -> Array("date:1980", "type:text"),
          "view" -> Array("table")))
        val qpl = FreemarkerUtil.createQueryParamList(params)
        
        it("should give back all query params") {
          qpl.getQueryParamList.size should equal (3)
        }

        it("should give back a filtered list when requested") {
          val queryParamFiltered = qpl.getListFiltered(true, Array("query"))
          queryParamFiltered.head.key should equal ("query")
          queryParamFiltered.size should equal (1)
        }

        it("should give back only default parameters") {
          val qp = qpl.getDefaultParams
          qp.size should equal (1)
          qp.head.key should equal ("view")
        }

        it("should format a List of QueryParam") {
          val sp = qpl.getSearchParams
          qpl.formatAsUrl(sp) should equal ("query=sjoerd&qf=date:1980&qf=type:text")
        }
  
      }
      
    }
  
}
