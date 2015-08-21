'''
Created on Jun 8, 2015

@author: pta
'''

from django.conf.urls import url

from . import views
from quiz.views import CathiDetailView, DethiStartView

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^login/$', views.login_user, name='login_user'),
    
    url(r'^cathi/(?P<pk>[\d]+)/$', CathiDetailView.as_view(), name="cathi_detail"),
    url(r'^cathi/(?P<pk>[\d]+)/start/$', DethiStartView.as_view(), name='dethi_start'),
    url(r'^cathi/(?P<pk>[\d]+)/start/finish/$', views.quiz_finish, name='quiz_finish'),
    
]