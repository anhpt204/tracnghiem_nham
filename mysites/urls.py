from django.conf.urls import patterns, include, url
import settings

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
                       #url(r'^hanusum/$', 'index'),
                       #url(r'^hanusum/detail/(?P<id>\d+)', 'detail'),
                       #url(r'^hanusum/detail/(?P<id>\d+)/(?P<name>[a-z]+)/$', 'detail'),
                       
    # Examples:
    # url(r'^$', 'mysites.views.home', name='home'),
    url(r'^hanusum/', include('hanusum.urls')),
    url(r'^polls/', include('polls.urls')),
    url(r'^blog/', include('blog.urls')),
    url(r'^vwn/', include('vwn.urls')),
    
    
    url(r'^q/', include('quiz.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),
)

if settings.DEBUG:
    urlpatterns += patterns('',
        (r'^vwn/(?P<path>.*)$', 'django.views.static.serve',  {'document_root': settings.MEDIA_ROOT.replace('\\','/')},),
    )