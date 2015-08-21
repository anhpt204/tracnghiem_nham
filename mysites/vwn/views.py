# Create your views here.
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.forms import ModelForm
from django import forms
from django.http import HttpResponseRedirect, HttpResponse
from django.core.context_processors import csrf
from django.template import loader, Context, RequestContext

# import rdflib

from vwn.models import Word, WordSense, Property, POS, Relation
# from ubuntuone.controlpanel.replication_client import exclude

# rdfs = rdflib.Namespace('http://protege.stanford.edu/2012/9/schema/')
# rdf = rdflib.Namespace('http://protege.stanford.edu/2012/9/vwn/instances/')


#class WordForm(ModelForm):
#    class Meta:
#        model = Word
def main(request):
    word = WordForm()
    type = POS.objects.all()
    d = dict(word = word, type=type)
    d.update(csrf(request))
    return render_to_response("main.html", d, context_instance=RequestContext(request))
    
def getSenses(request, word_id=0, pos_id = -1):
    '''
    get senses of a word that has id = word_id
    '''
    if(word_id > 0):
        #word searching
        word = Word.objects.get(pk=word_id)
        #get all pos
        type = POS.objects.all()
        #get pos search
        pos = type[0]
        if pos_id > -1:
            pos = POS.objects.filter(pk=pos_id)
        senses = WordSense()
        synonyms = []
        wf = WordForm()
        if word: #if has a word for searching
            #get all senses of searching word
            senses = WordSense.objects.filter(word=word).filter(type=pos).filter(is_validated=True).order_by('sensenr')
            for sense in senses:
                syn = sense.synset
                word_senses = WordSense.objects.filter(synset_id__exact=syn.id).filter(is_validated=True).exclude(word=sense.synset.word)
                synonyms.append(word_senses)
#        #get word from vwn.rdf
        relations = Property.objects.filter(is_validated=True)
        #relations = PropertyForm(instance=properties)
        d = dict(word = wf, type=type, senses=senses, synonyms = synonyms, relations = relations, word_search=word)
        d.update(csrf(request))
        return render_to_response("main.html", d, context_instance=RequestContext(request))

class WordForm(ModelForm):
    class Meta:
        model = Word
        exclude = ['created_date']
    
def search(request):
    '''
    search a word
    '''
    form = request.POST
    if form.has_key('lexform') and form['lexform']:
        w = form["lexform"]
        try:
            word = Word.objects.get(lexform__iexact = w)
            return HttpResponseRedirect(reverse("vwn.views.getSenses", args=[word.pk]))
        except:
            print 'no result'
    return HttpResponse("No result")


def PropertyForm(ModelForm):
    class Meta:
        model = Property
        widgets = {'choose': forms.RadioSelect()}    

        
def get_relations(request, rel_pk, syn_pk):
    relations = [] # [[wordsenses of one synset], []]
    wordsenses = WordSense.objects.filter(synset_id__exact=syn_pk).filter(is_validated=True)
    main_sense = wordsenses
    #relations.append(wordsenses)
    syn_temp = syn_pk
    rel_temp = rel_pk
    rel = Relation.objects.filter(synset1_id__exact=syn_temp, relation_id__exact=rel_temp, is_validated=True)
    display_type = False #hien thi cho quan he theo cau truc cay
    if len(rel) > 1:
        display_type =True #hien thi cho quan he theo cau truc Part of
        for re in rel:
            wordsenses = WordSense.objects.filter(synset_id__exact=re.synset2.pk).filter(is_validated=True)
            relations.append(wordsenses)
    else:
        while True:
            try:
                rel = Relation.objects.get(synset1_id__exact=syn_temp, relation_id__exact=rel_temp, is_validated=True)
                wordsenses = WordSense.objects.filter(synset_id__exact=rel.synset2.pk).filter(is_validated=True)
                relations.append(wordsenses)
                syn_temp = rel.synset2.pk
            except Exception as e:
                print '%s (%s)' % (e.message, type(e))
                break
        #get all wordsenses
    property = Property.objects.get(pk=rel_pk)
    d = dict(main_sense=main_sense, relations = relations, property=property, dis_type = display_type)
    d.update(csrf(request))
    return render_to_response("relation.html", d, context_instance=RequestContext(request))
    