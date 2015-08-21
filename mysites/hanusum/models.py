from django.db import models
from django import forms

# Create your models here.

class Summ(models.Model):
    url = models.URLField()
    text = models.TextField()
    sum = models.TextField()
    vote = models.IntegerField()
    
    