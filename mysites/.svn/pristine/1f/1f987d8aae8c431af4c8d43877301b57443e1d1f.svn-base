'''
Created on Sep 5, 2012

@author: pta
'''
from django.contrib import admin
from models import Item, DateTime

class ItemAdmin(admin.ModelAdmin):
    list_display = ["name", "priority","difficulty", "created", "done"]
    search_field = ["name"]
    
class ItemInline(admin.TabularInline):
    model = Item
    
class DateAdmin(admin.ModelAdmin):
    list_display = ["datetime"]
    inlines = [ItemInline]
    
admin.site.register(Item, ItemAdmin)
admin.site.register(DateTime, DateAdmin)