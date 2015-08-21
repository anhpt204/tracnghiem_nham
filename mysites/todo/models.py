from django.db import models
from django.template.defaultfilters import default

# Create your models here.
class DateTime(models.Model):
    datetime = models.DateTimeField(auto_now_add=True)
    def __unicode__(self):
        return unicode(self.datetime)
        
class Item(models.Model):
    created = models.ForeignKey(DateTime)
    name = models.CharField(max_length=60)
    priority = models.IntegerField(default=0)
    difficulty = models.IntegerField(default=0)
    done = models.BooleanField(default=False)
    
