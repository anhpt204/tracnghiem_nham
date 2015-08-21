# -*- coding: utf-8 -*-
from django.db import models
from django import forms

# Create your models here.
class Word(models.Model):
    lexform = models.CharField("Nhập từ", max_length=100, unique=True, help_text="Nhập một từ tiếng Việt")
    created_date = models.DateTimeField('Ngày tạo', auto_now_add=True)   
    is_validated = models.BooleanField("Xác nhận", default=False) 
    #gloss = models.TextField(max_length=200)
    class Meta:
        verbose_name_plural = ('Danh mục từ')
        permissions = (('can_change_is_validated', 'User can change is_validated field'),)
        ordering = ["-created_date"]
        
    def __unicode__(self):
        return self.lexform

class POS(models.Model):
    pos = models.CharField("Từ loại", max_length = 100, unique = True, help_text='Từ loại')
    is_validated = models.BooleanField("Xác nhận", default=False)     
    class Meta:
        verbose_name_plural = ('Từ loại')
        permissions = (('can_change_is_validated', 'User can change is_validated field'),)
        

    def __unicode__(self):
        return self.pos
           
class Synset(models.Model):
    word = models.ForeignKey(Word, verbose_name="Từ đầu tiên trong Synset")
    type = models.ForeignKey(POS, verbose_name='Từ loại')
    sensenr = models.IntegerField("Nghĩa thứ", help_text="Nghĩa thứ mấy của từ?") 
    gloss = models.TextField("Giải nghĩa", help_text='Ý nghĩa của Synset')
    example = models.TextField("Ví dụ") #, blank = True, null = True)
    is_validated = models.BooleanField("Xác nhận", default=False)     

    class Meta:
        verbose_name_plural = ('Định nghĩa Synset')
        permissions = (('can_change_is_validated', 'User can change is_validated field'),)
        #order_with_respect_to = 'word'
        ordering = ['word']

    def __unicode__(self):
        return self.word.lexform + '-' + self.type.pos.lower() + '-' + str(self.sensenr)
#    def __init__(self):
#        #lexform = models.ForeignKey(Word)
#        #lexform = models.CharField(max_length=20)
#        type = models.CharField(max_length = 5)
#        sensenr = models.IntegerField()
#        gloss = models.CharField(max_length=200)
#        hyponymOf = models.ForeignKey(self)
#        similarTo = models.ForeignKey(self)
#        memberMeronymOf = models.ForeignKey(self)
#        substanceMeronymOf = models.ForeignKey(self)
#        partMeronymOf = models.ForeignKey(self)
#        classifiedByTopic = models.ForeignKey(self)
#        causes = models.ForeignKey(self)
#        sameVerbGroupAs = models.ForeignKey(self)
#        attribute = models.ForeignKey(self)
#        adjectivePertainsTo = models.ForeignKey(self)
#        adverbPertainsTo = models.ForeignKey(self)
#        meronymOf = models.ForeignKey(self)
#        created_date = models.DateTimeField('date created')
    
class WordSense(models.Model):
    word = models.ForeignKey(Word, verbose_name="Từ")
        #lexform = models.CharField(max_length=20)
    type = models.ForeignKey(POS, verbose_name="Từ loại")
    sensenr = models.IntegerField("Nghĩa thứ")
    synset = models.ForeignKey(Synset, verbose_name="Thuộc synset")
    is_validated = models.BooleanField("Xác nhận", default=False)     
    created_date = models.DateTimeField('date created', auto_now_add=True)
    class Meta:
        verbose_name_plural = ('Các nghĩa của từ')
        ordering = ["word", "type", "sensenr"]
        unique_together = ("word", "type", "sensenr", "synset")
        permissions = (('can_change_is_validated', 'User can change is_validated field'),)

    def __unicode__(self):
        return self.word.lexform + '-' + self.type.pos.lower() + '-' + str(self.sensenr)
    
class Property(models.Model):
    name = models.CharField("Tên quan hệ", max_length=50, unique = True)
    domain = models.CharField("Loại 1", max_length=50, blank = True)
    arrange = models.CharField("Loại 2", max_length=50)
    text = models.CharField("Định nghĩa", max_length=100)
    is_validated = models.BooleanField("Xác nhận", default=False)     
    
    class Meta:
        verbose_name_plural = ('Định nghĩa quan hệ')
        permissions = (('can_change_is_validated', 'User can change is_validated field'),)

    def __unicode__(self):
        return self.text
    

class Relation(models.Model):
    #wordsense = models.ForeignKey(WordSense, blank=True)
    #synset1 relation synset2
    synset1 = models.ForeignKey(Synset, verbose_name="Synset thứ nhất", related_name='+')
    
    relation = models.ForeignKey(Property, verbose_name="Quan hệ")
    synset2 = models.ForeignKey(Synset, verbose_name="Synset thứ hai")
    is_validated = models.BooleanField("Xác nhận", default=False)     

    class Meta:
        verbose_name_plural = ('Quan hệ giữa các Synset')
        permissions = (('can_change_is_validated', 'User can change is_validated field'),)
        unique_together = ("synset1", "relation", "synset2")

    def __unicode__(self):
        return self.relation.name
    
#class WordForm(forms.Form):
#    word = forms.CharField(max_length=20)
    #gloss = forms.CharField(max_length=200)
    