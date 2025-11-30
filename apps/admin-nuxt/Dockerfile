# syntax=docker/dockerfile:1

ARG NUXT_PUBLIC_API_BASE
ARG NUXT_PRIVATE_API_BASE

FROM node:20-bookworm-slim AS deps
WORKDIR /app
COPY package.json .
RUN corepack enable && pnpm install

FROM node:20-bookworm-slim AS builder
ARG NUXT_PUBLIC_API_BASE
ARG NUXT_PRIVATE_API_BASE
RUN apt-get update && apt-get install -y --no-install-recommends python3 build-essential && rm -rf /var/lib/apt/lists/*
ENV NUXT_PUBLIC_API_BASE=${NUXT_PUBLIC_API_BASE}
ENV NUXT_PRIVATE_API_BASE=${NUXT_PRIVATE_API_BASE}
ENV NODE_OPTIONS=--max-old-space-size=4096
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN corepack enable && pnpm install --no-frozen-lockfile \
    && pnpm --filter admin-nuxt run build

FROM node:20-bookworm-slim AS runner
ARG NUXT_PUBLIC_API_BASE
ARG NUXT_PRIVATE_API_BASE
ENV NUXT_PUBLIC_API_BASE=${NUXT_PUBLIC_API_BASE}
ENV NUXT_PRIVATE_API_BASE=${NUXT_PRIVATE_API_BASE}
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/apps/admin-nuxt/.output ./.output
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
