# Create your views here.
from django.core.paginator import Paginator, InvalidPage, EmptyPage
from django.core.urlresolvers import reverse
from django.shortcuts import render_to_response
from django.forms import ModelForm
from django.http import HttpResponseRedirect, HttpResponse
from django.core.context_processors import csrf

from models import *

def main(request):
    posts = Post.objects.all().order_by("-created")
    paginator = Paginator(posts, 2)
    
    try:
        page = int(request.GET.get("page", '1'))
    except ValueError: page = 1
    
    try:
        posts = paginator.page(page)
    except (InvalidPage, EmptyPage):
        posts = paginator.page(paginator.num_pages)
    return render_to_response("blog/list.html", dict(posts = posts, user = request.user))    
    
def post(request, pk):
    post = Post.objects.get(pk = int(pk))
    comments = Comment.objects.filter(post=post)
    d = dict(post=post, comments=comments, form=CommentForm, user=request.user)
    d.update(csrf(request))
    return render_to_response("blog/post.html", d)

class CommentForm(ModelForm):
    class Meta:
        model = Comment
        exclude = ["post"]
        
def add_comment(request, pk):
    p = request.POST
    
    if p.has_key("body") and p["body"]:
        author = "Anonymous"
        if p["author"]:
            author = p["author"]
        comment = Comment(post=Post.objects.get(pk=pk))
        cf = CommentForm(p, instance=comment)
        cf.fields["author"].required = False
        
        comment = cf.save(commit=False)
        comment.author=author
        comment.save()
    return HttpResponseRedirect(reverse("blog.views.post", args=[pk]))
    #return HttpResponse(p["body"])