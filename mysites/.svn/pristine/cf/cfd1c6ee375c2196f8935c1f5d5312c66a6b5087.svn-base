'''
Created on Sep 26, 2012

@author: pta
'''
from django.conf.urls import patterns, include, url
from django.views.generic import DetailView, ListView
from django.views.static import serve
import settings

urlpatterns = patterns('vwn.views', 
                       url(r'^$','main'),
                       url(r'^search/$', 'search'),
                       url(r'^(\d+)/$', 'getSenses'),
                       url(r'^relation/(\d+)/(\d+)/$', 'get_relations'),
                       url(r'^search/(\d+)/(\d+)/$', 'getSenses'),
                       )
