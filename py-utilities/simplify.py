'''
Created on Nov 6, 2014

@author: tuananh
'''
from sympy import simplify, cos, sin, symbols, log, exp
from sympy.core.function import count_ops


X1, X2 = symbols('X1 X2')

#SC - K4 - size 86
#a = (cos(exp(X1)) / (cos((cos(cos(((X1 * X1) + cos(cos(cos((X1 * log(exp(X1))))))))) + (exp(X1) + cos(cos((((sin(exp(X1)) / X1) + exp((exp(X1) * (X1 - X1)))) + (exp(X1) * (X1 - X1)))))))) + (cos((cos(cos(((sin(exp(X1)) / X1) + exp((exp(X1) * (X1 - X1)))))) + (exp(X1) + cos(cos(((X1 * X1) + cos(cos(cos((X1 * log(exp(X1)))))))))))) + ((cos((cos((sin(cos((exp(X1) + exp((exp(X1) * (X1 - X1)))))) / X1)) + (exp(X1) + exp((exp(X1) * (X1 - X1)))))) + (exp(X1) + (exp(X1) / X1))) * exp(cos((X1 * log(exp(X1)))))))))

#SC - K12 - size 38
#a = ((sin(X1) * ((X1 / cos(X2)) + cos((sin(X1) * (sin((log(exp(sin(X1))) - log(X1))) + sin((sin((((X1 / cos((X1 - log(X1)))) - log(X1)) - log(X1))) + sin((log(exp((sin(sin(X1)) / cos(log(X1))))) - (X2 - X2)))))))))) - (X1 + X2))

#SGXMSC K4 - size 135
a = (((1 / ((X1 / X1) + exp((0 - (1 / (1 * (X1 - X1))))))) * sin((((exp(((log(X1) - X1) - (sin(X1) - sin(sin(((X1 * X1) + (X1 * X1))))))) / X1) - sin((X1 + X1))) / (((cos(exp(X1)) + (1 - ((1 / (1 + exp((sin((X1 + X1)) - cos(X1))))) / (1 + exp((X1 - exp(X1))))))) + (X1 / (1 / (1 + exp((1 - ((X1 * X1) - ((1 / sin(X1)) / 1)))))))) - (sin((X1 - X1)) + log(X1))))) * (sin(exp(cos(((sin((X1 + X1)) + X1) / X1)))) / 1)) + ((1 - (1 / (1 + exp(((X1 / X1) - (sin(X1) - sin((X1 + X1)))))))) * sin(((sin(X1) - sin((X1 + X1))) / (((cos(X1) + (1 - ((1 / (1 + exp((sin((X1 * X1)) - cos(X1))))) / (1 + exp((sin((X1 + 1)) - (X1 * X1))))))) + (X1 / (1 / (1 + exp((cos(X1) - ((X1 * X1) - (((X1 / X1) / (X1 - X1)) / (X1 / exp(X1)))))))))) - (sin((X1 - X1)) + log(X1))))) * sin(exp(cos((1 + sin((X1 + X1))))))))
    

#SGXMSC K12 - size 128
#a = (((1 - (1 / (1 + exp((0 - cos((((X1 / X1) - X1) - cos((X1 * X1))))))))) * (((1 - (X1 / (cos(log(exp((((X1 / X1) - X1) - cos(X2))))) + exp((X1 - 0))))) * (log(exp((((X1 / X1) - X1) - cos((X1 * X1))))) - X2)) + (X1 * (X1 - X2) * sin(1)))) + ((1 / (1 + exp((0 - cos(sin(((1 / (1 + exp((cos(X2) - (cos(X1) / (X1 * X1)))))) * (X1 - (((sin(X1) / X1) - X1) - cos((X1 * X1)))) * sin((X1 - (1 / sin(1))))))))))) * ((1 - (1 / (1 + exp(((X1 - X1) - X2))))) * (((1 - ((sin(log(exp(cos(X1)))) / (1 + exp(exp(X1)))) / ((1 - (1 / (1 + exp(((X1 - X1) - X2))))) + exp((cos((X1 - X1)) - (X1 / (X1 - 0))))))) * (log(exp((((X1 / X1) - X1) - cos(X2)))) - X2)) + ((1 / (1 + exp((cos(sin(X1)) - (cos(X1) / (X1 * X1)))))) * (X1 - X2) * sin((1 - (1 / exp((((X1 / X1) - X1) - cos((X1 * X1)))))))))) * (cos((X1 * X1)) * (X1 * X1) * 0)))



#print a

y = simplify(a)

print 'size after simplify: ', count_ops(y)

print y
