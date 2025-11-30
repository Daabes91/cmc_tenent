import Image from "next/image"
import Link from "next/link"

export default function Logo() {
  return (
    <Link
      href="/"
      className="flex items-center gap-2 font-bold text-xl focus:outline-none focus:ring-2 focus:ring-primary/60 rounded-md"
    >
  <div className="flex h-9 w-9 items-center justify-center overflow-hidden rounded-md border border-primary/10 bg-transparent shadow-none">
  <Image
    src="/brand-logo.png"
    alt="CMC Platform logo"
    width={100}
    height={100}
    className="h-9 w-9 object-contain"
    priority
  />
</div>
      <span className="text-primary">Cliniqax</span>
    </Link>
  )
}
