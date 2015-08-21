# Create your views here.

from django.http import HttpResponse, HttpResponseRedirect
from django.core.urlresolvers import reverse
from django.forms import ModelForm
from django.template import loader, Context
from django.core.context_processors import csrf
from django.shortcuts import render_to_response

from hanusum.models import *

class SummForm(forms.ModelForm):
    class Meta:
        model = Summ
        
def summ_view(request, pk=0):
    form = SummForm()
    if pk:
        summ = Summ.objects.get(pk=int(pk))
        summ.sum = "text summarization"
        summ.save()
        form = summ
    d = dict(form = form)
    d.update(csrf(request))
    return render_to_response('summ.html', d)

def vnsum_view(request):
    t = loader.get_template('vnsum.html')
    c = Context()
    return HttpResponse(t.render(c))

def index(request):
    return HttpResponse("<h1>HOME PAGE</h1><HR>")

def detail(request, id, name):
    response = HttpResponse()
    response.write('<h1>Detail</h1></HR>')
    response.write("<h2>ID: %s " %id)
    response.write('Name: %s</h2' %name)
    return response

def summ_url(request):
    form = SummForm({'url':'vnexpress.net', 'sum':'text summarization'})
    d = dict(form = form)
    d.update(csrf(request))
    return render_to_response('summ.html', d)
    
def summarise(request):
    p = request.POST
    if p.has_key('url'):
        summ = Summ()
        sf = SummForm(p, instance=summ)
        sf.fields["text"].required = False
        sf.fields["vote"].required = False
        sf.fields["sum"].required = False
        summ = sf.save(commit=False)
        summ.vote=0
        summ.text=""
        summ.sum=""
        summ.save()
        return HttpResponseRedirect(reverse("hanusum.views.summ_view", args=[summ.pk]))
    return HttpResponse("Error")