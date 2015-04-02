/*
* Source: TweetNaCl (http://tweetnacl.cr.yp.to/20140427/tweetnacl.c)
*/

#include "curve25519.h"

typedef unsigned char u8;
typedef long long int i64;
typedef i64 gf[16];

static const gf _121665 = {0xDB41,1};
static const u8 _9[32] = {9};

static void carry(gf o)
{
  int i;
  i64 c;
  for(i=0;i<16;i++) {
    o[i]+=(1LL<<16);
    c=o[i]>>16;
    o[(i+1)*(i<15)]+=c-1+37*(c-1)*(i==15);
    o[i]-=c<<16;
  }
}

static void select(gf p,gf q,int b)
{
  i64 t,i,c=~(b-1);
  for(i=0;i<16;i++) {
    t= c&(p[i]^q[i]);
    p[i]^=t;
    q[i]^=t;
  }
}

static void pack(u8 *o,const gf n)
{
  int i,j,b;
  gf m,t;
  for(i=0;i<16;i++) t[i]=n[i];
  carry(t);
  carry(t);
  carry(t);
  for(j=0;j<2;j++) {
    m[0]=t[0]-0xffed;
    for(i=1;i<15;i++) {
      m[i]=t[i]-0xffff-((m[i-1]>>16)&1);
      m[i-1]&=0xffff;
    }
    m[15]=t[15]-0x7fff-((m[14]>>16)&1);
    b=(m[15]>>16)&1;
    m[14]&=0xffff;
    select(t,m,1-b);
  }
  for(i=0;i<16;i++) {
    o[2*i]=t[i]&0xff;
    o[2*i+1]=t[i]>>8;
  }
}

static void unpack(gf o, const u8 *n)
{
  int i;
  for(i=0;i<16;i++) o[i]=n[2*i]+((i64)n[2*i+1]<<8);
  o[15]&=0x7fff;
}

static void A(gf o,const gf a,const gf b)
{
  int i;
  for(i=0;i<16;i++) o[i]=a[i]+b[i];
}

static void Z(gf o,const gf a,const gf b)
{
  int i;
  for(i=0;i<16;i++) o[i]=a[i]-b[i];
}

static void M(gf o,const gf a,const gf b)
{
  i64 i,j,t[31];
  for(i=0;i<31;i++) t[i]=0;
  for(i=0;i<16;i++)
    for(j=0;j<16;j++)
      t[i+j]+=a[i]*b[j];
  for(i=0;i<15;i++) t[i]+=38*t[i+16];
  for(i=0;i<16;i++) o[i]=t[i];
  carry(o);
  carry(o);
}

static void S(gf o,const gf a)
{
  M(o,a,a);
}

static void invert(gf o,const gf i)
{
  gf c;
  int a;
  for(a=0;a<16;a++) c[a]=i[a];
  for(a=253;a>=0;a--) {
    S(c,c);
    if(a!=2&&a!=4) M(c,c,i);
  }
  for(a=0;a<16;a++) o[a]=c[a];
}

int crypto_scalarmult(u8 *q,const u8 *n,const u8 *p)
{
  u8 z[32];
  i64 x[80],r,i;
  gf a,b,c,d,e,f;
  for(i=0;i<31;i++) z[i]=n[i];
  z[31]=(n[31]&127)|64;
  z[0]&=248;
  unpack(x,p);
  for(i=0;i<16;i++) {
    b[i]=x[i];
    d[i]=a[i]=c[i]=0;
  }
  a[0]=d[0]=1;
  for(i=254;i>=0;--i) {
    r=(z[i>>3]>>(i&7))&1;
    select(a,b,r);
    select(c,d,r);
    A(e,a,c);
    Z(a,a,c);
    A(c,b,d);
    Z(b,b,d);
    S(d,e);
    S(f,a);
    M(a,c,a);
    M(c,b,e);
    A(e,a,c);
    Z(a,a,c);
    S(b,a);
    Z(c,d,f);
    M(a,c,_121665);
    A(a,a,d);
    M(c,c,a);
    M(a,d,f);
    M(d,b,x);
    S(b,e);
    select(a,b,r);
    select(c,d,r);
  }
  for(i=0;i<16;i++) {
    x[i+16]=a[i];
    x[i+32]=c[i];
    x[i+48]=b[i];
    x[i+64]=d[i];
  }
  invert(x+32,x+32);
  M(x+16,x+16,x+32);
  pack(q,x+16);
  return 0;
}

int crypto_scalarmult_base(u8 *q,const u8 *n)
{ 
  return crypto_scalarmult(q,n,_9);
}