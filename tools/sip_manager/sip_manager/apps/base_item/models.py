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
import hashlib

from django.db import models
from django.contrib import admin

from utils.gen_utils import dict_2_django_choice

from apps.dummy_ingester.models import Request



def to_str(text):
    """ Convert 'text' to a `str` object.

        >>> to_str(u'I\xf1t\xebrn\xe2ti')
        'I\xc3\xb1t\xc3\xabrn\xc3\xa2ti'
        >>> to_str(42)
        '42'
        >>> to_str('ohai')
        'ohai'
        >>> class Foo:
        ...     def __str__(self):
        ...         return 'foo'
        ...
        >>> f = Foo()
        >>> to_str()
        'foo'
        >>> f.__unicode__ = lambda: u'I\xf1t\xebrn\xe2ti'
        >>> to_str(f)
        'I\xc3\xb1t\xc3\xabrn\xc3\xa2ti'
        >>> """
    if isinstance(text, str):
        return text

    if hasattr(text, '__unicode__'):
        text = text.__unicode__()

    if hasattr(text, '__str__'):
        return text.__str__()

    return text.encode('utf-8')


def calculate_mdr_content_hash(record):
    """
    When calculating the content hash for the record, the following is asumed:
      the lines are stripped for initial and trailing whitespaces,
      sorted alphabetically
      each line is separated by one \n character
    """
    #s = to_str(record)
    r_hash = hashlib.sha256(record.upper()).hexdigest().upper()
    return r_hash





# MDRS_ = MdRecord state
MDRS_CREATED = 1
MDRS_IDLE = 2
MDRS_PROCESSING = 3
MDRS_PROBLEMATIC = 4
MDRS_BROKEN = 5
MDRS_VERIFIED = 6

MDRS_STATES = {
    MDRS_CREATED: 'created',
    MDRS_IDLE: 'idle',
    MDRS_PROCESSING: 'processing',
    MDRS_PROBLEMATIC: 'problematic',
    MDRS_BROKEN: 'broken',
    MDRS_VERIFIED: 'verified',
    }

class MdRecord(models.Model):
    content_hash = models.CharField(max_length=100)

    # source data is the original record, treated in the following way:
    #   each line from the file is stripped of initial and trailing whitespace
    #   then concatenated with one \n char, no trailing \n
    source_data = models.TextField()
    status = models.IntegerField(choices=dict_2_django_choice(MDRS_STATES),
                                 default = MDRS_CREATED)
    time_created = models.DateTimeField(auto_now_add=True,editable=False)
    time_last_change = models.DateTimeField(auto_now_add=True,editable=False)

    pid = models.IntegerField(default=0) # what process 'owns' this item
    uniqueness_hash = models.CharField(max_length=100)
    Enrichment_done = models.BooleanField(default=False)

admin.site.register(MdRecord)




class RequestMdRecord(models.Model):
    request = models.ForeignKey(Request)
    md_record = models.ForeignKey(MdRecord)
    i = models.IntegerField(default=0, blank=True)

admin.site.register(RequestMdRecord)
