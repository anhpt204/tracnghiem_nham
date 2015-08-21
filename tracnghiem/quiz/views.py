'''
Created on Jun 8, 2015

@author: pta
'''
from django.http import HttpResponse
from django.contrib.auth.views import logout
from django.contrib.auth import authenticate, login
from django.http.response import HttpResponseRedirect
from django.shortcuts import render_to_response
from django.template.context import RequestContext
from quiz.models import DMMonThi, CaThi, DeThi, Question, Answer
from django.template import loader
from django.views.decorators.csrf import csrf_protect
from datetime import date, datetime
from django.views.generic.detail import DetailView
# from numpy.distutils.from_template import template_name_re
from django.views.generic.list import ListView
import json
from quiz import ESSAYQUESTION, TFQUESTION
from django.utils.datastructures import MultiValueDictKeyError

def index(request):
    return HttpResponse("Hello, world. You're at the polls index.")

def login_user(request):
    logout(request)
    username = password = ''
    
#     ds_mothi = DMMonThi.objects.all();
    today = datetime.now().date()
    ds_cathi = CaThi.objects.filter(ngay_thi=today)
    template = loader.get_template('login.html')
    context = RequestContext(request, {
        'ds_cathi': ds_cathi,
    })
    
    if(request.POST):
        username = request.POST['username']
        password = request.POST['password']
        cathi_id = request.POST['cathi']
         
        user = authenticate(username=username, password=password)
         
        if user is not None:
            login(request, user)
            
            dethi = DeThi.objects.filter(sinh_vien__ma_sv=username, ca_thi=cathi_id)[0]
            return HttpResponseRedirect('/quiz/cathi/' + str(dethi.id) + '/')
         
    return HttpResponse(template.render(context))

def quiz_finish(request, pk):
    dethi = DeThi.objects.get(pk=pk)

    answers = {}
    
    if request.POST:
        questions = json.loads(dethi.ds_cau_hoi)
        for question in questions:
            q_id = str(question[0])
            try:
                answers[question[0]] = int(request.POST[q_id])
            except:
                continue
            
    dethi.user_answers = json.dumps(answers)
    
    dethi.save()
    return HttpResponse('Tinhs diem')

class CathiDetailView(DetailView):
    model = DeThi
    template_name='cathi_detail.html'
#     pk_url_kwarg = 'cathi'
    
#     def get(self, request, *args, **kwargs):
#         return DetailView.get(self, request, *args, **kwargs)


class DethiStartView(DetailView):
    model = DeThi
    template_name = 'dethi_start.html'
    
#     def get(self, request, *args, **kwargs):
#         object = DetailView.get(self, request, *args, **kwargs)
#         
#         context = self.get_context_data(object=self.object)
        
    def get_context_data(self, **kwargs):
        context = DetailView.get_context_data(self, **kwargs)
        
                
        context['questions'] = self.object.get_ds_cau_hoi()
        
        return context
        
        
