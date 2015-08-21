'''
Created on Sep 27, 2012

@author: pta
'''

from vwn.models import Word, Synset, WordSense, POS, Property, Relation
from django.contrib import admin
#import reversion
from django.contrib.admin.models import LogEntry, DELETION
from django.utils.html import escape
from django.core.urlresolvers import reverse

#Override modeladmin
class _ModelAdmin(admin.ModelAdmin):
    def get_form(self, request, obj=None, **kwargs):
        if hasattr(self, 'field_permissions'):
            user = request.user
            for _field in self.opts.fields:
                perm = self.field_permissions.get(_field.name)
                if perm and not user.has_perm(perm):
                    if self.exclude:
                        self.exclude.append(_field.name)
                    else:
                        self.exclude=[_field.name]
        return super(_ModelAdmin, self).get_form(request, obj, **kwargs)

class WordSenseInline(admin.StackedInline):
    model = WordSense
    extra = 2
    
class SynsetInline(admin.TabularInline):
    model = Synset
    extra = 2
    
class WordAdmin(_ModelAdmin):#admin.ModelAdmin):#reversion.VersionAdmin):#
    inlines = [WordSenseInline]
    list_display = ['lexform', 'created_date']
    search_fields = ['lexform']
    date_hierarchy = 'created_date'
    list_filter = ['created_date']
    field_permissions = {'is_validated':'word.can_change_is_validated'}

class WordSenseAdmin(_ModelAdmin):#admin.ModelAdmin):#reversion.VersionAdmin):#    
    #search_fields = ['word']
    date_hierarchy = 'created_date'
    list_display = ['id', 'word', 'type', 'sensenr', 'synset']
    #list_display_links = ['word', 'synset']
    #list_filter = ['word', 'type', 'synset']
    field_permissions = {'is_validated':'wordsense.can_change_is_validated'}
    
class SynsetAdmin(_ModelAdmin):# admin.ModelAdmin):#reversion.VersionAdmin):#
    list_display = ['id', 'word', 'type', 'sensenr', 'gloss', 'example', 'is_validated']
    field_permissions = {'is_validated':'synset.can_change_is_validated'}
    
class RelationAdmin(_ModelAdmin):#admin.ModelAdmin):
    list_display = ['id', 'synset1', 'relation', 'synset2']
    list_filter = ['synset1', 'relation', 'synset2']
    field_permissions = {'is_validated':'relation.can_change_is_validated'}
    
class POSAdmin(_ModelAdmin):
    field_permissions = {'is_validated':'pos.can_change_is_validated'}
    
class PropertyAdmin(_ModelAdmin):
    list_display = ['name', 'text']
    field_permissions = {'is_validated':'relation.can_change_is_validated'}
    

#------------------------------------------------------------------------------ 
#Log
class LogEntryAdmin(admin.ModelAdmin):
    date_hierarchy = 'action_time'
#    readonly_fields = LogEntry._meta.get_all_field_names()
    list_filter = [
        'user',
        'content_type',
        'action_flag'
    ]

    search_fields = [
        'object_repr',
        'change_message'
    ]


    list_display = [
        'action_time',
        'user',
        'content_type',
        'object_link',
        'action_flag',
        'change_message',
    ]

    def has_add_permission(self, request):
        return False

    def has_change_permission(self, request, obj=None):
        return request.user.is_superuser and request.method != 'POST'

    def has_delete_permission(self, request, obj=None):
        return False

    def object_link(self, obj):
        if obj.action_flag == DELETION:
            link = escape(obj.object_repr)
        else:
            ct = obj.content_type
            link = u'<a href="%s">%s</a>' % (
                reverse('admin:%s_%s_change' % (ct.app_label, ct.model), args=[obj.object_id]),
                escape(obj.object_repr),
            )
        return link
    object_link.allow_tags = True
    object_link.admin_order_field = 'object_repr'
    object_link.short_description = u'object'


admin.site.register(LogEntry, LogEntryAdmin)
#------------------------------------------------------------------------------ 
    
    
admin.site.register(POS, POSAdmin)
admin.site.register(Property, PropertyAdmin)
admin.site.register(Word, WordAdmin)
admin.site.register(Relation, RelationAdmin)
admin.site.register(Synset, SynsetAdmin)
admin.site.register(WordSense, WordSenseAdmin)

