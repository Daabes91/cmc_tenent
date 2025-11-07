# syntax=docker/dockerfile:1

ARG NUXT_PUBLIC_API_BASE
ARG NUXT_PRIVATE_API_BASE

FROM node:20-alpine AS deps
WORKDIR /app
COPY pnpm-lock.yaml .
COPY apps/admin-nuxt/package.json .
RUN corepack enable && pnpm install --no-frozen-lockfile

FROM node:20-alpine AS builder
ARG NUXT_PUBLIC_API_BASE
ARG NUXT_PRIVATE_API_BASE
ENV NUXT_PUBLIC_API_BASE=${NUXT_PUBLIC_API_BASE}
ENV NUXT_PRIVATE_API_BASE=${NUXT_PRIVATE_API_BASE}
ENV NODE_OPTIONS=--max-old-space-size=4096
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY apps/admin-nuxt .
RUN corepack enable && pnpm install --no-frozen-lockfile && pnpm run build

FROM node:20-alpine AS runner
ARG NUXT_PUBLIC_API_BASE
ARG NUXT_PRIVATE_API_BASE
ENV NUXT_PUBLIC_API_BASE=${NUXT_PUBLIC_API_BASE}
ENV NUXT_PRIVATE_API_BASE=${NUXT_PRIVATE_API_BASE}
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/.output ./.output
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
