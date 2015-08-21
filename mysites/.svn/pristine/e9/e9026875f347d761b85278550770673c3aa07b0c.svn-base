'''
Created on Sep 5, 2012

@author: pta
'''
from django.contrib import admin
from models import *

class PostAdmin(admin.ModelAdmin):
    search_fields = ["title"]
    
class CommentAdmin(admin.ModelAdmin):
    display_fields = ["post", "author", "created"]
    
admin.site.register(Post, PostAdmin)
admin.site.register(Comment, CommentAdmin)