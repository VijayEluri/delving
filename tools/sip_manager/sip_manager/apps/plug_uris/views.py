"""
 Copyright 2010 EDL FOUNDATION

 Licensed under the EUPL, Version 1.1 or as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 you may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.


 Created by: Jacob Lundqvist (Jacob.Lundqvist@gmail.com)



"""

# Create your views here.

from django.db import connection
from django.db.models import Q
from django.shortcuts import render_to_response, get_object_or_404

from apps.dummy_ingester.models import Request

#from datagrids import UriSourcesDataGrid
import models

Q_OBJECT = Q(item_type=models.URIT_OBJECT)
Q_OK = Q(status=models.URIS_COMPLETED, err_code=models.URIE_NO_ERROR)
Q_BAD = ~Q(err_code=models.URIE_NO_ERROR)

"""

myapp/datagrid.html


reviews/dashboard.html
eta_history = []

def delta_waiting():
    global eta_history
    t0 = time.time()
    waiting = uri_summary(){'imgs_waiting'}
    if eta_history:
        latest_t, latest_waiting = eta_history[0]
        oldest_t, oldest_waiting = eta_history[-1]
    else:
        return -1
    if (t0 - latest_t) > 10:
        eta_history.insert(0, (t0, waiting))
    eta_history.insert((t0, waiting), 0)
    t0, old_waiting = eta_history.pop
    percent_done = float(step) / self._task_steps * 100
    elapsed_time = time.time() - self._task_time_start
    eta_t_from_now = int(elapsed_time / ((percent_done / 100) or 0.001))
    eta = self._task_time_start + eta_t_from_now
    if (eta - time.time()) < SHOW_DATE_LIMIT:
        eta_s = time.strftime('%H:%M:%S', time.localtime(eta))
    else:
        eta_s = time.strftime('%m-%d %H:%M:%S', time.localtime(eta))


"""

def dg1(request, template_name='plug_uris/datagrid1.html'):
    return UriSourcesDataGrid(request).render_to_response(template_name)

def statistics(request):
    return render_to_response("plug_uris/statistics.html", {
        'summary': uri_summary(),})

def stats_req_lst(request):
    lst = []
    for req in  models.ReqUri.objects.values('req').distinct():
        req_id = req['req']
        q_all = Q(req=req_id, item_type=models.URIT_OBJECT)
        qs_all = models.ReqUri.objects.filter(q_all)
        count = qs_all.count()
        #itm_ok = models.ReqUri.objects.filter(q_all, Q_OK).count()
        itm_ok = qs_all.filter(Q_OK).count()
        #itm_bad = models.ReqUri.objects.filter(q_all, Q_BAD).count()
        itm_bad = qs_all.filter(Q_BAD).count()
        waiting = count - itm_ok - itm_bad
        lst.append({'request':Request.objects.get(pk=req_id),
                   'count': count,
                   'waiting': waiting,
                   'ok': itm_ok,
                   'bad': itm_bad,
                   })

    return render_to_response("plug_uris/stats_all_requests.html", {
        'requests': lst,})


def stats_by_req(request, sreq_id=0):
    req_id = int(sreq_id)
    request = models.Request.objects.filter(pk=req_id)[0]

    #
    # Grouped by mimetype
    #
    qs_all = models.ReqUri.objects.filter(Q_OBJECT, req=req_id)

    mime_results = []
    for row in qs_all.values_list('mime_type').distinct().order_by('mime_type'):
        mime_type = row[0]
        if not mime_type:
            continue # not set for this item
        qs_mime = qs_all.filter(mime_type=mime_type)
        itm_ok = qs_mime.filter(Q_OK).count()
        itm_bad = qs_mime.filter(Q_BAD).count()
        mime_results.append({'name':mime_type,
                             'ok': itm_ok,
                             'bad': itm_bad,
                             'ratio': s_calc_ratio(itm_ok, itm_bad),
                             })

    #
    # Grouped by error
    #
    err_by_reason = {}
    for err_code in models.URI_ERR_CODES.keys():
        if err_code == models.URIE_NO_ERROR:
            continue
        count = qs_all.filter(err_code=err_code).count()
        if not count:
            continue
        err_by_reason[models.URI_ERR_CODES[err_code]] = count

    #
    # Grouped by webserver
    #
    webservers = []
    for row in qs_all.values_list('source_id').distinct().order_by('source_id'):
        srv_id = int(row[0])
        srv_name = models.UriSource.objects.get(pk=srv_id)
        qs_webserver = qs_all.filter(source_id=srv_id)
        items = qs_webserver.count()
        good = qs_webserver.filter(Q_OK).count()
        bad = qs_webserver.filter(Q_BAD).count()
        webservers.append({'name': srv_name,
                           'srv_id': srv_id,
                           'count' :items,
                           'good': good,
                           'bad': bad,
                           'ratio': s_calc_ratio(good, bad),
                           })

    return render_to_response("plug_uris/stats_by_request.html",
                              {
                                  'request': request,
                                  'mime_results': mime_results,
                                  'err_by_reason': err_by_reason,
                                  'webservers': webservers,
                              })


def stats_by_uri(request, order_by=''):
    if order_by:
        if order_by in ('name_or_ip','imgs_waiting','imgs_ok','imgs_bad','eta'):
            if request.session.get('sortkey','').find(order_by)==1:
                # that is if it was a -key we change it to key
                p_order_by = order_by
            else:
                p_order_by = '-%s' % order_by
        else:
            p_order_by = 'name_or_ip'
    else:
        p_order_by = 'name_or_ip'
    request.session['sortkey'] = p_order_by

    qs_all = models.ReqUri.objects.filter(Q_OBJECT)

    uri_sources = []
    web_servers = models.UriSource.objects.values_list('name_or_ip', 'pk').order_by('name_or_ip')
    for name, source_id in web_servers:
        qs_web_server = qs_all.filter(source_id=source_id)
        count = qs_web_server.count()
        good = qs_web_server.filter(Q_OK).count()
        bad = qs_web_server.filter(Q_BAD).count()
        waiting = count - good - bad
        uri_sources.append({'name': name,
                            'id':source_id,
                            'waiting': waiting,
                            'good': good,
                            'bad': bad,
                            'ratio': s_calc_ratio(good, bad)})

    return render_to_response("plug_uris/stats_uri_source.html", {
        "uri_sources":uri_sources,
        "summary": uri_summary(),})


def problems(request, source_id=-1):
    try:
        urisource = models.UriSource.objects.get(pk=source_id)
    except:
        urisource = None
    problems = {}
    for k in models.URI_ERR_CODES:
        if k == models.URIE_NO_ERROR:
            continue
        uri_filter = {'err_code': k}
        if urisource:
            uri_filter['uri_source'] = urisource
        count = models.Uri.objects.filter(**uri_filter).count()
        if not count:
            continue
        problems[models.URI_ERR_CODES[k]] = {
            'err_code': k,
            'count': count,
        }
    return render_to_response('plug_uris/problems.html', {
        'urisource': urisource,
        'problems': problems})



def uri_summary():
    imgs_ok = models.ReqUri.objects.filter(item_type=models.URIT_OBJECT,
                                           status=models.URIS_COMPLETED,
                                           ).count()
    imgs_waiting = models.ReqUri.objects.filter(item_type=models.URIT_OBJECT,
                                                 status=models.URIS_CREATED).count()
    imgs_bad = models.ReqUri.objects.filter(item_type=models.URIT_OBJECT).exclude(err_code=models.URIE_NO_ERROR).count()
    return {"imgs_ok": imgs_ok,
            "imgs_waiting": imgs_waiting,
            "imgs_bad": imgs_bad}




def index(request):
    uris =  models.Uri.objects.filter(status=models.URIS_CREATED,
                                      pid=0).order_by('-uri_source')

    return render_to_response('plug_uris/index.html', {'uri':uris[0]})





#
# Util funcs
#

def s_calc_ratio(good, bad):
    return '%0.2f' % calc_ratio(good, bad)

def calc_ratio(good, bad):
    if not good:
        # avoid divide by zero
        return 0
    return 100-bad/float(good) * 100



#
# Obsolete
#

def OLD_stats_req_lst(request):
    sql_waiting = ["AND u.status=%i AND u.err_code=%i" % (models.URIS_CREATED,
                                                     models.URIE_NO_ERROR)]
    sql_ok = ["AND u.status=%i AND u.err_code=%i" % (models.URIS_COMPLETED,
                                                     models.URIE_NO_ERROR)]
    sql_err = ["AND u.err_code>0"]
    cursor = connection.cursor()
    lst = []
    for req in  models.ReqUri.objects.values('req').distinct():
        req_id = req['req']

        sql = ["SELECT COUNT(*)"]
        sql.append("FROM plug_uris_requri ur, plug_uris_uri u")
        sql.append("WHERE ur.req_id=%i" % req_id)
        sql.append("AND u.id=ur.uri_id")
        sql.append("AND u.item_type=%i" % models.URIT_OBJECT)

        #whops this would count all item types - not what we want here...
        #count =  models.ReqUri.objects.filter(req__pk=req_id).count()
        cursor.execute(' '.join(sql))
        count = cursor.fetchone()[0]
        cursor.execute(' '.join(sql + sql_ok))
        itm_ok = cursor.fetchone()[0]
        cursor.execute(' '.join(sql + sql_err))
        itm_bad = cursor.fetchone()[0]
        waiting = count - itm_ok - itm_bad
        lst.append({'request':Request.objects.get(pk=req_id),
                   'count': count,
                   'waiting': waiting,
                   'ok': itm_ok,
                   'bad': itm_bad,
                   })

    return render_to_response("plug_uris/stats_all_requests.html", {
        'requests': lst,})



def Old_stats_by_req(request, sreq_id=0):
    req_id = int(sreq_id)
    request = models.Request.objects.filter(pk=req_id)[0]
    cursor1 = connection.cursor()
    cursor2 = connection.cursor()

    #
    # Status by mimetype
    #
    """
    SELECT DISTINCT u.mime_type FROM plug_uris_requri ur, plug_uris_uri u
    WHERE ur.uri_id=u.id and ur.req_id=1
    """
    sql = ["SELECT DISTINCT u.mime_type"]
    sql.append("FROM plug_uris_requri ur, plug_uris_uri u")
    sql.append("WHERE ur.req_id=%i" % req_id)
    sql.append("AND u.id=ur.uri_id")
    sql.append("AND u.item_type=%i" % models.URIT_OBJECT)
    cursor1.execute(' '.join(sql))
    sql_ok = ["AND u.status=%i AND u.err_code=%i" % (models.URIS_COMPLETED,
                                                     models.URIE_NO_ERROR)]
    sql_err = ["AND u.err_code>0"]
    mime_results = {}
    for row in cursor1.fetchall():
        mime_type = row[0]
        if not mime_type:
            continue
        sql = ["SELECT COUNT(*)"]
        sql.append("FROM plug_uris_requri ur, plug_uris_uri u")
        sql.append("WHERE ur.req_id=%i" % req_id)
        sql.append("AND ur.uri_id=u.id AND u.mime_type LIKE '%s'" % mime_type)
        sql.append("AND u.item_type=%i" % models.URIT_OBJECT)
        cursor2.execute(' '.join(sql + sql_ok))
        itm_ok = cursor2.fetchone()[0]
        cursor2.execute(' '.join(sql + sql_err))
        itm_bad = cursor2.fetchone()[0]
        mime_results[mime_type] = {'ok': itm_ok,
                                   'bad': itm_bad}

    #
    # Generate list of webservers for this request
    #
    sql = ["SELECT DISTINCT us.id, us.name_or_ip"]
    sql.append("FROM plug_uris_requri ur, plug_uris_uri u, plug_uris_urisource us")
    sql.append("WHERE ur.req_id=%i" % req_id)
    sql.append("AND u.id=ur.uri_id")
    sql.append("AND u.item_type=%i" % models.URIT_OBJECT)
    sql.append("AND us.id=u.uri_source_id")
    cursor1.execute(' '.join(sql))
    webservers = []
    for row in cursor1.fetchall():
        srv_id = int(row[0])
        srv_name = row[1]
        sql = ["SELECT COUNT(*) FROM plug_uris_requri ur, plug_uris_uri u"]
        sql.append("WHERE u.uri_source_id=%i" % srv_id)
        sql.append("AND u.item_type=%i" % models.URIT_OBJECT)
        sql.append("AND ur.uri_id=u.id AND ur.req_id=%i" % req_id)
        cursor2.execute(' '.join(sql))
        items = cursor2.fetchone()[0]
        webservers.append((srv_name, srv_id, items))
    webservers.sort()

    #
    # Bad by reason
    #
    err_by_reason = {}
    for err_code in models.URI_ERR_CODES.keys():
        if err_code == models.URIE_NO_ERROR:
            continue
        sql = ["SELECT COUNT(*)"]
        sql.append("FROM plug_uris_uri u, plug_uris_requri ur")
        sql.append("WHERE ur.req_id=%i" % req_id)
        sql.append("AND u.id=ur.uri_id AND u.err_code=%i" % err_code)
        cursor1.execute(' '.join(sql))
        count = cursor1.fetchone()[0]
        if not count:
            continue
        err_by_reason[models.URI_ERR_CODES[err_code]] = count
    return render_to_response("plug_uris/stats_by_request.html",
                              {
                                  'request': request,
                                  'mime_results': mime_results,
                                  'webservers': webservers,
                                  'err_by_reason': err_by_reason,
                              })



def OLD_uri_summary():
    imgs_ok = models.Uri.objects.filter(status=models.URIS_COMPLETED).count()
    imgs_waiting = models.Uri.objects.filter(status=models.URIS_CREATED,
                                             item_type=models.URIT_OBJECT,
                                             err_code=models.URIE_NO_ERROR).count()
    imgs_bad = models.Uri.objects.exclude(err_code=models.URIE_NO_ERROR).count()
    return {"imgs_ok": imgs_ok,
            "imgs_waiting": imgs_waiting,
            "imgs_bad": imgs_bad}
def OLD_stats_by_uri(request, order_by=''):
    if order_by:
        if order_by in ('name_or_ip','imgs_waiting','imgs_ok','imgs_bad','eta'):
            if request.session.get('sortkey','').find(order_by)==1:
                # that is if it was a -key we change it to key
                p_order_by = order_by
            else:
                p_order_by = '-%s' % order_by
        else:
            p_order_by = 'name_or_ip'
    else:
        p_order_by = 'name_or_ip'
    request.session['sortkey'] = p_order_by

    sel_common = "SELECT COUNT(*) FROM plug_uris_uri WHERE"
    sql_table_join = "AND uri_source_id=plug_uris_urisource.id"

    sql_img_ok = "%s status=%i %s" % (sel_common, models.URIS_COMPLETED, sql_table_join)
    sql_img_waiting = "%s status=%i AND item_type=%i AND err_code=%i %s" % (
        sel_common, models.URIS_CREATED, models.URIT_OBJECT, models.URIE_NO_ERROR, sql_table_join)
    sql_img_bad = "%s err_code > %i %s" % (sel_common, models.URIE_NO_ERROR, sql_table_join)

    sql = "SELECT task_eta FROM process_monitor_processmonitoring where pid=plug_uris_urisource.pid"
    uri_sources = models.UriSource.objects.extra(select={
        'imgs_ok':sql_img_ok,
        'imgs_bad': sql_img_bad,
        'imgs_waiting':sql_img_waiting,
        #'eta':sql,
        }).order_by(p_order_by)

    return render_to_response("plug_uris/stats_uri_source.html", {
        "uri_sources":uri_sources,
        "summary": uri_summary(),})
