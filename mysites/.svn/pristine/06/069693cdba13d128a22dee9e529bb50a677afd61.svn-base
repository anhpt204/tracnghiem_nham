'''
Created on Aug 1, 2012

@author: pta
'''
from django.conf.urls import patterns, include, url

urlpatterns = patterns('hanusum.views',
                       url(r'^$', 'summ_view'),
                       url(r'^(\d+)/$', 'summ_view'),
                       url(r'vnsum/$', 'vnsum_view'),
                       url(r'^summarise/$', 'summarise'),
                       #url(r'^hanusum/detail/(?P<id>\d+)', 'detail'),
                       #url(r'^hanusum/detail/(?P<id>\d+)/(?P<name>[a-z]+)/$', 'detail'),
                       
    # Examples:
    # url(r'^$', 'mysites.views.home', name='home'),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    # url(r'^admin/', include(admin.site.urls)),
)
