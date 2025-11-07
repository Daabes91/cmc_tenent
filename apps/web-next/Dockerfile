# syntax=docker/dockerfile:1

ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL

FROM node:20-alpine AS deps
WORKDIR /app
COPY pnpm-lock.yaml .
COPY apps/web-next/package.json .
RUN corepack enable && pnpm install --no-frozen-lockfile

FROM node:20-alpine AS builder
ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_SERVER_API_URL=${NEXT_SERVER_API_URL}
ENV NODE_OPTIONS=--max-old-space-size=4096
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY apps/web-next .
RUN corepack enable && pnpm install --no-frozen-lockfile && pnpm run build

FROM node:20-alpine AS runner
ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_SERVER_API_URL=${NEXT_SERVER_API_URL}
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./package.json
COPY --from=deps /app/node_modules ./node_modules
EXPOSE 3001
CMD ["npm", "run", "start"]
