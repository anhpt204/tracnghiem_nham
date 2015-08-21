'''
Created on Aug 30, 2012

@author: pta
'''
from polls.models import Poll, Choice
from django.contrib import admin

class ChoiceInline(admin.TabularInline):
    model = Choice
    extra = 3
    
class PollAdmin(admin.ModelAdmin):
    #ordering fields
    #fields = ['pub_date', 'question']
    
    #grouping fields
    fieldsets = [
                 (None, {'fields':['question']}),
                 ('Date information', {'fields':['pub_date'], 'classes':['collapse']})
                 ]
    inlines=[ChoiceInline]
    list_display = ('question', 'pub_date', 'was_published_recently')
    list_filter = ['pub_date']
    search_fields = ['question']
    date_hierarchy = 'pub_date'

admin.site.register(Poll, PollAdmin)
#admin.site.register(Choice)