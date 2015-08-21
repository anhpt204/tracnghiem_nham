'''
Created on Sep 6, 2012

@author: pta
'''
from django.conf.urls import patterns, include, url
from django.views.generic import DetailView, ListView

urlpatterns = patterns('blog.views', 
                       url(r'^$','main'),
                       url(r'^(\d+)/$', 'post'),
                       url(r'^add_comment/(\d+)\$', 'add_comment'),
                       )